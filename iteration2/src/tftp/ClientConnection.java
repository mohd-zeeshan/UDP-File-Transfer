package tftp;

import java.io.*;
import java.net.*;

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
	 * @param receiveSocket		
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
		do {
			Packet ack = new ACKPacket(ACKPacket.getBlockFromInt(block), packet.getAddress(), packet.getPort());
			send(sendReceiveSocket, ack.getPacket());
			receive(sendReceiveSocket, aPacket);
			byte[] content = FileHandler.trim(DataPacket.getDataFromPacket(aPacket));
			FileHandler.writeToFile(Server.SERVER_PATH+RequestPacket.getFilename(packet), content);
			block++;
		} while(!DataPacket.isLastPacket(aPacket));
		// send last ACK
		Packet ack = new ACKPacket(ACKPacket.getBlockFromInt(block), packet.getAddress(), packet.getPort());
		send(sendReceiveSocket, ack.getPacket());
	}

	/**
	 * Handles Read request by sending content of the file in chunks of 512 bytes and 
	 * receiving ACK.
	 * 
	 * @param packet
	 */
	private void handleRRQ(DatagramPacket packet) {
		try {
		    File file = new File(Server.SERVER_PATH + RequestPacket.getFilename(packet));
		    FileInputStream in = new FileInputStream(file);
		    int chunkLen = 0;
		    int block = 1;		    
		    do {
				byte[] data = new byte[Packet.DATA_PACKET_SIZE-4];
				chunkLen = in.read(data);
				Packet dataPacket = new DataPacket(block, FileHandler.trim(data), packet.getAddress(), packet.getPort());
				send(sendReceiveSocket, dataPacket.getPacket());
				if(FileHandler.trim(data).length < 512) {
					break;
				}
				if(receiveACK(block)) {
					System.out.println("Correct ACK received!\n");
				} else {
					System.out.println("Wrong ACK received!\n");
					break;
				}
				block++;
		    } while (chunkLen != -1);
		    receiveACK(block);		// receive the last ACK
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
		System.out.print(">");
	}

}
