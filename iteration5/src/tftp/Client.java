package tftp;

import java.io.*;
import java.net.*;
import java.nio.file.AccessDeniedException;
import java.util.Scanner;

import packet.*;

/**
 * Implementation of the client. Sends and receives packet to and from the intermediate host.
 * 
 * @author Dhrubo
 *
 */
public class Client {
	
	private static final int CLIENT_TIMEOUT = 5000;
	private DatagramSocket sendReceiveSocket, rrqSocket;
	private DatagramPacket  receivePacket;
	public static int CLIENT_PORT = 23;
	private static final String CLIENT_PATH = "test_files/client/";
	private static final String DEFAULT_MODE = "netascii";
	private boolean rrqSuccessful, wrqSuccessful;
	private InetAddress hostAddress;
	
	/**
	 * Constructor for Client class. Creates a socket for sending and receiving.
	 */
	public Client() {
		rrqSuccessful = false;
		try {
			sendReceiveSocket = new DatagramSocket();
			rrqSocket = new DatagramSocket();
			sendReceiveSocket.setSoTimeout(CLIENT_TIMEOUT);
		} catch (SocketException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Reads filename from the server.
	 * @param filename	the name of the file we want to read
	 */
	public void read(String filename) {
		System.out.println("Sending RRQ...");
//			InetAddress hostAddress = InetAddress.getByName(hostAddressStr);
		Packet request = new ReadRequestPacket(filename, DEFAULT_MODE, hostAddress, CLIENT_PORT);
//			Packet request = new ReadRequestPacket(filename, DEFAULT_MODE, hostAddress, Server.SERVER_PORT);
		send(rrqSocket, request.getPacket());
		receiveDataAndSendACK(filename);
		if(rrqSuccessful) {
			System.out.println("File read successful! Saved at location: " + CLIENT_PATH + filename + "\n");
		} else {
			System.out.println("File read failed!");
			File file = new File(CLIENT_PATH + filename);
			if(file.exists()) file.delete();
		}
	}
	
	/**
	 * Receives data from host and sends ACK packet.
	 * A block of data < 512 bytes terminates the transfer.
	 * 
	 * @param filename
	 */
	private void receiveDataAndSendACK(String filename) {
		boolean fileChecked = false;
		do {
			long usableSpace = new File(System.getProperty("user.dir")).getUsableSpace();
			// If we have enough space left, go on writing to file, other wise send error packet and terminate
			if(usableSpace > Packet.DATA_PACKET_SIZE-4) {
				receive(rrqSocket);
				if(!Packet.isDATA(receivePacket)) {
					rrqSuccessful = false;
					break;
				}
				File file = new File(CLIENT_PATH + filename);
				// If file already exist, then send Error packet 6
				if(!fileChecked && file.exists()) {
					fileChecked = true;
					String errMsg = "A file named '" + filename + "' already exists in client!";
					Packet errorPacket = new ErrorPacket(6, errMsg.getBytes(), receivePacket.getAddress(), receivePacket.getPort());
					send(rrqSocket, errorPacket.getPacket());
					System.out.println("Client says: " + errMsg + "\nTerminating...\n");
					break;
				}
				if(Packet.isERROR(receivePacket)) {
					rrqSuccessful = false;
					System.out.println("Client says: ERROR Packet received with message: " + new String(this.receivePacket.getData()));
					System.out.println("Quiting...\n");
					break;
				}
				FileHandler.writeToFile(CLIENT_PATH + filename, FileHandler.trim(DataPacket.getDataFromPacket(receivePacket)));
				byte[] blockNum = ACKPacket.getBlock(receivePacket);
				Packet ack = new ACKPacket(blockNum, receivePacket.getAddress(), receivePacket.getPort());
				System.out.println("DATA received. Sending ACK...");
				send(rrqSocket, ack.getPacket());
				rrqSuccessful = true;
				fileChecked = true;
			} else {
				String errMsg = "Disk full. Only " + usableSpace + " byte space left";
				Packet errorPacket = new ErrorPacket(3, errMsg.getBytes(), receivePacket.getAddress(), receivePacket.getPort());
				send(rrqSocket, errorPacket.getPacket());
				System.out.println("Client says: " + errMsg + "\nTerminating...\n");
				break;
			}
		} while(!DataPacket.isLastPacket(receivePacket));
	}
	
	/**
	 * Writes file to the server
	 * @param filename	the name of the file we want to read
	 */
	public void write(String filename) {
		if(!(new File(CLIENT_PATH + filename)).exists()) {
			System.err.println("Client: File '" + CLIENT_PATH + filename + "' does not exist.\nNot sending WRQ\nTerminating...");
			System.exit(1);
		}
		System.out.println("Sending WRQ...");
//			InetAddress hostAddress = InetAddress.getByName(hostAddressStr);
		Packet request = new WriteRequestPacket(filename, DEFAULT_MODE, hostAddress, CLIENT_PORT);
//			Packet request = new WriteRequestPacket(filename, DEFAULT_MODE, hostAddress, Server.SERVER_PORT);
		send(request.getPacket());
		sendData(filename);
		if(wrqSuccessful) {
			System.out.println("File write successful! Saved at location: " + Server.SERVER_PATH + filename + "\n");
		}
	}
	
	/**
	 * Sends contents of 'filename' in chunks to the host and verifies ACK packets.
	 * @param filename
	 */
	private void sendData(String filename) {
	    try {
			File file = new File(CLIENT_PATH + filename);
			FileInputStream in = new FileInputStream(file);
			int chunkLen = 0;
			int block = 0;
			boolean breakOut = false;
			Packet dataPacket = null;
			do {
				// Receive data
				byte[] data = new byte[Packet.DATA_PACKET_SIZE];
				receivePacket = new DatagramPacket(data, data.length);
			    System.out.println("Client says: Waiting for Packet from host...");
			    try {
			    	sendReceiveSocket.receive(receivePacket);
			    } catch (SocketTimeoutException e) {
			    	System.out.println("\n*** Timeout of 5 seconds occured ***\nRE_TRANSMITTING...Sending again!\n");
					send(dataPacket.getPacket());
					sendReceiveSocket.receive(receivePacket);
				}
				Packet.printRequest(receivePacket);
				System.out.println("Packet Recieved!\n");
				
				
				if(Packet.isACK(receivePacket)) {
					if(receivedCorrectACK(block)) {
						System.out.println("Correct ACK received!\n");
					} else {
						System.out.println("Wrong ACK received!\n");
						break;
					}
				} else if(Packet.isERROR(receivePacket)) {
					wrqSuccessful = false;
					System.out.println("Client says: ERROR Packet received with message: " + new String(receivePacket.getData(), 4, receivePacket.getLength()));
					System.out.println("Terminating...\n");
					break;
				}
				if(breakOut) break;
				data = new byte[Packet.DATA_PACKET_SIZE - 4];
				chunkLen = in.read(data);
				byte[] content = FileHandler.trim(data);
				breakOut = content.length < 512;
				dataPacket = new DataPacket(block, content, receivePacket.getAddress(), receivePacket.getPort());
				send(dataPacket.getPacket());
				block++;
				wrqSuccessful = true;
			} while(chunkLen != -1);
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (AccessDeniedException e) {
			System.out.println("File Access violation: " + filename );
	    	System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Receives ACK packet and checks whether or not correct ACK received
	 * @param block
	 * @return	whether correct ACK packet received
	 */
	private boolean receivedCorrectACK(int block) {
		return Packet.getBlockNumber(receivePacket) == block;
	}

	/**
	 * Sends packet via socket parameter
	 * @param socket
	 * @param packet
	 */
	private void send(DatagramSocket socket, DatagramPacket packet) {
		try {
			System.out.println("Client says: Sending packet to host...");
			Packet.printRequest(packet);
			socket.send(packet);
			System.out.println("Client: Packet sent.\n");
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}	
	}
	
	/**
	 * Send packet via sendReceiveSocket
	 * @param packet
	 */
	private void send(DatagramPacket packet) {
		send(this.sendReceiveSocket, packet);
	}
	
	/**
	 * Waits on its DatagramSocket. When it receives a DatagramPacket from the intermediate host, 
	 * it prints out the information received, including the byte array.
	 * 
	 */
	private void receive(DatagramSocket socket) {
	    try {
			byte[] data = new byte[Packet.DATA_PACKET_SIZE];
			receivePacket = new DatagramPacket(data, data.length);
		    System.out.println("Client says: Waiting for Packet from host...");
			socket.receive(receivePacket);
			Packet.printRequest(receivePacket);
			System.out.println("Packet Recieved!\n");
	    } catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
	    }
	}
	
	private void printUsage() {
		System.out.println("Usage:");
		
		System.out.println("\n  - Read a file:");
		System.out.println("      read [filename] [host address]");
		System.out.println("        e.g. 'read server_big.txt 192.168.46.1'");
		
		System.out.println("\n  - Write a file:");
		System.out.println("      write [filename] [host address]");
		System.out.println("        e.g. 'write client_big.txt 192.168.46.1'");
		
		System.out.println("\n  - Shut down client:");
		System.out.println("      exit");
	}
	
	/**
	 * Takes user input. Asks if user wants to read or write file. Then asks for the filename
	 * 
	 */
	public void takeInput() {
		Scanner in = new Scanner(System.in);
		printUsage();
		while(true) {
			System.out.print("\n> ");
			String s = in.nextLine();
			try {
				if(s.startsWith("read")) {	
					String[] parts = s.split(" ");
					String filename = parts[1];
					this.hostAddress = parts.length < 3 ? InetAddress.getLocalHost() : InetAddress.getByName(parts[2]);
					System.out.println("Reading " + filename + " from server...");
					read(filename);
				} else if(s.startsWith("write")) {	
					String[] parts = s.split(" ");
					String filename = parts[1];
					this.hostAddress = parts.length < 3 ? InetAddress.getLocalHost() : InetAddress.getByName(parts[2]);
					System.out.println("Writing " + filename + " to server...");
					write(filename);
				} else if(s.equals("exit")) {
					System.out.println("Shutting down client!");
					break;
				}
			} catch (UnknownHostException e) {
				e.printStackTrace();
				System.exit(1);
			}
			
		}
		in.close();
		System.out.println("Done.");
	}
		
	public static void main(String[] args) {
		Client c = new Client();
		c.takeInput();
//		c.write("client_big.txt");
//		c.read("server_big.txt");
	}

}
