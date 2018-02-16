package tftp;

import java.io.*;
import java.net.*;
import java.nio.file.AccessDeniedException;

import packet.*;

/**
 * ClientConnection class implements Runnable and responds to the requests it receives from host.
 *
 */

public class ClientConnection implements Runnable {

	private DatagramPacket receivePacket;
	private DatagramSocket sendReceiveSocket;
	
	/**
	 * Constructor for class ClientConnection
	 * @param receivePacket
	 */
	public ClientConnection(DatagramPacket receivePacket) {
		try {
			this.sendReceiveSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		this.receivePacket = receivePacket;
	}
	
	/**
	 * Waits to receive a request and prints out the information it has received.
	 * @param socket	
	 * @param packet
	 */
	public static void receive(DatagramSocket socket, DatagramPacket packet) {
	    try {
		    System.out.println("Server says: Waiting for Packet from host...");
    		socket.receive(packet);
    		Packet.printRequest(packet);
		    System.out.println("Packet Recieved!\n");
	    } catch (IOException e) {
    		e.printStackTrace();
    		System.exit(1);
	    } catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Determines if RRQ or WRQ and handles the requests.
	 * @param packet
	 */
	public void handleRQ(DatagramPacket packet) {
		if(Packet.isReadRequest(packet)) {
			System.out.println("RRQ received...\n");
			handleRRQ(packet);
		} else if(Packet.isWriteRequest(packet)) {
			System.out.println("WRQ received...\n");
			handleWRQ(packet);
		}
	}
	
	/**
	 * Handles Write requests by sending ACK and receiving data. Then writes to the appropriate file.
	 * 
	 * @param packet
	 */
	private void handleWRQ(DatagramPacket packet) {
		int block = 0;
		byte data[] = new byte[Packet.DATA_PACKET_SIZE];
	    DatagramPacket aPacket = new DatagramPacket(data, data.length);   
		String filename = Server.SERVER_PATH + RequestPacket.getFilename(packet);
		File file = new File(filename);
		if(file.exists()) {
			String errMsg = filename + "' already exists in server!";
			Packet errorPacket = new ErrorPacket(6, errMsg.getBytes(), packet.getAddress(), packet.getPort());
			send(sendReceiveSocket, errorPacket.getPacket());
			System.out.println("Server says: " + errMsg + "\nTerminating...\n");
		} else {
			do {
				long usableSpace = new File(System.getProperty("user.dir")).getUsableSpace();
				// If we have enough space left, go on writing to file, other wise send error packet and terminate
				if(usableSpace > Packet.DATA_PACKET_SIZE-4) {
					Packet ack = new ACKPacket(ACKPacket.getBlockFromInt(block), packet.getAddress(), packet.getPort());
					send(sendReceiveSocket, ack.getPacket());
					receive(sendReceiveSocket, aPacket);
					byte[] content = DataPacket.getDataFromPacket(aPacket);
					FileHandler.writeToFile(filename, content);
					block++;
				} else {
					String errMsg = "Disk full. Only " + usableSpace + " byte space left";
					Packet errorPacket = new ErrorPacket(3, errMsg.getBytes(), packet.getAddress(), packet.getPort());
					send(sendReceiveSocket, errorPacket.getPacket());
					System.out.println("Server says: " + errMsg + "\nTerminating...\n");
					break;
				}
			} while(!DataPacket.isLastPacket(aPacket));
			// send last ACK
			System.out.println("Sending Last ACK packet!");
			Packet ack = new ACKPacket(ACKPacket.getBlockFromInt(block), packet.getAddress(), packet.getPort());
			send(sendReceiveSocket, ack.getPacket());
		}
	}

	/**
	 * Handles Read request by sending content of the file in chunks of 512 bytes and 
	 * receiving ACK.
	 * 
	 * @param packet
	 */
	private void handleRRQ(DatagramPacket packet) {
		String filename = Server.SERVER_PATH + RequestPacket.getFilename(packet);
		try {
		    File file = new File(filename);
		    FileInputStream in = new FileInputStream(file);
		    int chunkLen = 0;
		    int block = 1;		    
		    do {
				byte[] data = new byte[Packet.DATA_PACKET_SIZE - 4];
				chunkLen = in.read(data);
				Packet dataPacket = new DataPacket(block, FileHandler.trim(data), packet.getAddress(), packet.getPort());
				send(sendReceiveSocket, dataPacket.getPacket());
				
				if(FileHandler.trim(data).length < 512) {
					break;
				}
				
				byte receivedData[] = new byte[500];
			    DatagramPacket dp = new DatagramPacket(receivedData, receivedData.length);
				receive(sendReceiveSocket, dp);
				
				if(Packet.isACK(dp)) {
					if(Packet.getBlockNumber(dp) == block) {
						System.out.println("Correct ACK received!\n");
					} else {
						System.out.println("Wrong ACK received!\n");
						break;
					}
				} else if(Packet.isERROR(dp)) {
					System.out.println("Server says: ERROR Packet received with message: " + new String(dp.getData(), 4, dp.getLength()));
					System.out.println("Terminating...\n");
					break;
				}
				block++;
		    } while (chunkLen != -1);
		    receiveACK(block);		// receive the last ACK
		    in.close();
		} catch (FileNotFoundException e) {
			String errMsg = "File '" + filename + "' does not exist!";
			ErrorPacket ep = new ErrorPacket(1, errMsg.getBytes(), packet.getAddress(), packet.getPort());
			send(sendReceiveSocket, ep.getPacket());
			System.out.println(errMsg + "\nConnection terminated!\n");
		} catch (AccessDeniedException e) {
			String errMsg = "Access violation on file '" + filename + "'!";
			ErrorPacket ep = new ErrorPacket(2, errMsg.getBytes(), packet.getAddress(), packet.getPort());
			send(sendReceiveSocket, ep.getPacket());
			System.out.println("Connection terminated!\n");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Receives ACK and determines if its the right one.
	 * @param block
	 * @return	Whether correct ACK received
	 */
	private boolean receiveACK(int block) {
		byte data[] = new byte[4];
	    DatagramPacket dp = new DatagramPacket(data, data.length);
		receive(sendReceiveSocket, dp);
		return Packet.getBlockNumber(dp) == block;
	}

	/**
	 * Creates a socket for sending packet back to the intermediate host and prints the necessary info.
	 * 
	 * @param packet			DatagramPacket received from host
	 * @throws Exception		
	 */
	private void send(DatagramSocket socket, DatagramPacket packet) {
		try {
			System.out.println("Server says: Sending packet to host...");
			RequestPacket.printRequest(packet);
			socket.send(packet);
			System.out.println("Packet sent!\n");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}


	@Override
	public void run() {
		handleRQ(receivePacket);
		System.out.println("Enter 'exit' to shut down server OR enter 'c' to continue...");
		System.out.print("> ");
	}

}
