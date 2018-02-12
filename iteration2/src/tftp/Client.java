package tftp;

import java.io.*;
import java.net.*;
import java.util.Scanner;

import packet.*;

/**
 * Implementation of the client. Sends and receives packet to and from the intermediate host.
 * 
 * @author Dhrubo
 *
 */
public class Client {
	
	private DatagramSocket sendReceiveSocket;
	private DatagramPacket sendPacket, receivePacket;
	public static int CLIENT_PORT = 23;
	private static final String CLIENT_PATH = "test_files/client/";
	private static final String DEFAULT_MODE = "netascii";
	private boolean rrqSuccessful;
	
	/**
	 * Constructor for Client class. Creates a socket for sending and receiving.
	 */
	public Client() {
		rrqSuccessful = false;
		try {
			sendReceiveSocket = new DatagramSocket();
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
		try {
			System.out.println("Sending RRQ...");
//			Packet request = new ReadRequestPacket(filename, DEFAULT_MODE, InetAddress.getLocalHost(), CLIENT_PORT);
			Packet request = new ReadRequestPacket(filename, DEFAULT_MODE, InetAddress.getLocalHost(), Server.SERVER_PORT);
			send(request);
			receiveDataAndSendACK(filename);
			if(rrqSuccessful) {
				System.out.println("File read successful! Saved at location: " + CLIENT_PATH + filename + "\n");
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
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
			receive();
			File file = new File(CLIENT_PATH + filename);
			if(!fileChecked && file.exists()) {
				fileChecked = true;
				String errMsg = "A file named '" + filename + "' already exists in client!";
				Packet errorPacket = new ErrorPacket(6, errMsg.getBytes(), receivePacket.getAddress(), receivePacket.getPort());
				send(errorPacket);
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
			send(ack);
			rrqSuccessful = true;
			fileChecked = true;
		} while(!DataPacket.isLastPacket(receivePacket));
	}
	
	/**
	 * Writes file to the server
	 * @param filename	the name of the file we want to read
	 */
	public void write(String filename) {
		try {
			System.out.println("Sending WRQ...");
//			Packet request = new WriteRequestPacket(filename, DEFAULT_MODE, InetAddress.getLocalHost(), CLIENT_PORT);
			Packet request = new WriteRequestPacket(filename, DEFAULT_MODE, InetAddress.getLocalHost(), Server.SERVER_PORT);
			send(request);
			sendData(filename);
			System.out.println("File write successful! Saved at location: " + Server.SERVER_PATH + filename + "\n");
		} catch (UnknownHostException e) {
			e.printStackTrace();
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
			do {
				if(receivedCorrectACK(block)) {
					System.out.println("Correct ACK received!\n");
				} else {
					System.out.println("Wrong ACK received!\n");
					break;
				}
				if(breakOut) break;
				byte[] data = new byte[Packet.DATA_PACKET_SIZE-4];
				chunkLen = in.read(data);
				byte[] content = FileHandler.trim(data);
				breakOut = content.length < 512 ? true : false;
				Packet dataPacket = new DataPacket(block, content, receivePacket.getAddress(), receivePacket.getPort());
				send(dataPacket);
				block++;
			} while(chunkLen != -1);
			in.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
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
		receive();
		return Packet.isACK(receivePacket) 
				&& Packet.getBlockNumber(receivePacket)==block;
	}

	/**
	 * Sends packet via sendReceiveSocket
	 * @param packet
	 */
	public void send(Packet packet) {
		try {
			sendPacket = packet.getPacket();
			System.out.println("Client says: Sending packet to host...");
			Packet.printRequest(sendPacket);
			sendReceiveSocket.send(sendPacket);
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
	 * Waits on its DatagramSocket. When it receives a DatagramPacket from the intermediate host, 
	 * it prints out the information received, including the byte array.
	 * 
	 */
	private void receive() {
	    try {
			byte[] data = new byte[Packet.DATA_PACKET_SIZE];
			receivePacket = new DatagramPacket(data, data.length);
		    System.out.println("Client says: Waiting for Packet from host...");
			sendReceiveSocket.receive(receivePacket);
			Packet.printRequest(receivePacket);
			System.out.println("Packet Recieved!\n");
	    } catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
	    }
	}
	
	/**
	 * Takes user input. Asks if user wants to read or write file. Then asks for the filename
	 * 
	 */
	public void takeInput() {
		Scanner in = new Scanner(System.in);
		while(true) {
			System.out.println("Type and enter either of following:");
			System.out.println("    1 : To read from server");
			System.out.println("    2 : To write to server");
			System.out.println("    exit : To shut down client");
			System.out.print(">");
			String s = in.nextLine();
			if(s.equals("1")) {
				System.out.println("Enter the filename you want to read from: ");
				System.out.print(">");
				String filename = in.nextLine();
				System.out.println("Reading " + filename + " from server...");
				read(filename);
			} else if(s.equals("2")) {
				System.out.println("Enter the filename you want to write: ");
				String filename = in.nextLine();
				System.out.println("Writing " + filename + " to server...");
				System.out.print(">");
				write(filename);
			} else if(s.equals("exit")) {
				System.out.println("Shutting down client!");
				break;
			}
		}
		in.close();
		System.out.println("Done.");
	}
		
	public static void main(String[] args) {
		Client c = new Client();
//		c.takeInput();
		c.read("server_big.txt");
	}

}
