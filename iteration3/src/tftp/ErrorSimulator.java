package tftp;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;

import packet.Packet;
import packet.RequestPacket;

/**
 * Implementation of the ErrorSimulator which is used only to communicate between the Client and the Server (for now).
 * 
 * @author Dhrubo
 *
 */
public class ErrorSimulator {
	
	private DatagramSocket receiveSocket, sendReceiveSocket;
	private DatagramPacket receiveFromClientPacket, sendToServerPacket, 
							receiveFromServerPacket, sendToClientPacket;
	private int clientPort;
	private InetAddress clientAddress;
	private InetAddress localHost;
	
	private Mode mode;
	private PacketType packetType;
	private enum Mode { NORMAL, LOSE, DELAY, DUPLICATE };
	private int blockNumber = -1;
	private enum PacketType { DATA, ACK };
	
	/**
	 * Constructor for Host class. Creates a socket for receiving, another socket for both
	 * sending and receiving.
	 * 
	 */
	public ErrorSimulator() {
		try {
			receiveSocket = new DatagramSocket(Client.CLIENT_PORT);
			sendReceiveSocket = new DatagramSocket();
			localHost = InetAddress.getLocalHost();
		} catch (SocketException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private void send(DatagramSocket socket, DatagramPacket packet) {
		try {
			socket.send(packet);
			RequestPacket.printRequest(packet);
			System.out.println("Host: Packet sent.\n");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private void receive(DatagramSocket socket, DatagramPacket packet) {
		try {
			socket.receive(packet);
			RequestPacket.printRequest(packet);
			System.out.println("Packet Recieved!\n");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Transfer RRQ or WRQ between client and server (port 69)
	 */
	private void transferRQ() {
		// Receive from client 
		byte data[] = new byte[Packet.DATA_PACKET_SIZE];
	    receiveFromClientPacket = new DatagramPacket(data, data.length);
	    System.out.println("ErrorSimulator says: Waiting for Packet from client...");
    	receive(receiveSocket, receiveFromClientPacket);
		clientPort = receiveFromClientPacket.getPort();
		clientAddress = receiveFromClientPacket.getAddress();

		// Send To Server (69)
		sendToServerPacket = new DatagramPacket(receiveFromClientPacket.getData(), receiveFromClientPacket.getLength(), localHost, Server.SERVER_PORT);
		System.out.println("ErrorSimulator says: Sending packet to server...");
		send(sendReceiveSocket, sendToServerPacket);

		// Receive From Server
		data = new byte[Packet.DATA_PACKET_SIZE];
	    receiveFromServerPacket = new DatagramPacket(data, data.length, localHost, Server.SERVER_PORT);
	    System.out.println("ErrorSimulator says: Waiting for Packet from server...");
    	receive(sendReceiveSocket, receiveFromServerPacket);
	}
	
	private boolean isDataPacketLoseMode() {
		return this.mode == Mode.LOSE && this.packetType == PacketType.DATA 
				&& Packet.isDATA(sendToServerPacket)
	    		&& Packet.getBlockNumber(sendToServerPacket) == this.blockNumber;
	}
	
	private void handleLoseDataPacketMode(InetAddress serverThreadAddress, int serverThreadPort) {
		System.out.println("\nNot sending " + packetType + " packet with block #" + blockNumber + " to server!\n");
		// Receive from client 
		byte[] data = new byte[Packet.DATA_PACKET_SIZE];
	    receiveFromClientPacket = new DatagramPacket(data, data.length, clientAddress, clientPort);
	    System.out.println("ErrorSimulator says: Waiting for Packet from client...");
    	receive(sendReceiveSocket, receiveFromClientPacket);
    	// Send To Server (client connection thread)
		sendToServerPacket = new DatagramPacket(receiveFromClientPacket.getData(), receiveFromClientPacket.getLength(), serverThreadAddress, serverThreadPort);
		System.out.println("ErrorSimulator says: Sending packet to server...");
    	send(sendReceiveSocket, sendToServerPacket);
	}
	
	public void simulateErrors() {
		while(true) {
		    transferRQ();
		    int serverThreadPort = receiveFromServerPacket.getPort();
			InetAddress serverThreadAddress = receiveFromServerPacket.getAddress();
		    
		    while(true) {
		    	// Send To Client
				sendToClientPacket = new DatagramPacket(receiveFromServerPacket.getData(), receiveFromServerPacket.getLength(), clientAddress, clientPort);	
				System.out.println("ErrorSimulator says: Sending packet to client...");
				send(sendReceiveSocket, sendToClientPacket);

				// Receive from client 
				byte[] data = new byte[Packet.DATA_PACKET_SIZE];
			    receiveFromClientPacket = new DatagramPacket(data, data.length, clientAddress, clientPort);
			    System.out.println("ErrorSimulator says: Waiting for Packet from client...");
		    	receive(sendReceiveSocket, receiveFromClientPacket);

		    	// Send To Server (client connection thread)
				sendToServerPacket = new DatagramPacket(receiveFromClientPacket.getData(), receiveFromClientPacket.getLength(), serverThreadAddress, serverThreadPort);
				System.out.println("ErrorSimulator says: Sending packet to server...");
				if(isDataPacketLoseMode()) {
					handleLoseDataPacketMode(serverThreadAddress, serverThreadPort);
			    } else {
			    	send(sendReceiveSocket, sendToServerPacket);
			    }

				// Receive From Server (client connection thread)
				data = new byte[Packet.DATA_PACKET_SIZE];
			    receiveFromServerPacket = new DatagramPacket(data, data.length, serverThreadAddress, serverThreadPort);
			    System.out.println("ErrorSimulator says: Waiting for Packet from server...");
		    	receive(sendReceiveSocket, receiveFromServerPacket);
				
		    }
		}
	}
	
	private void printHelp() {
		System.out.println("Usage:");
		System.out.println("  - Normal Operation:\n      0 ");
		
		System.out.println("\n  - Lose a packet:");
		System.out.println("      1 [Block #] [DATA or ACK]");
		System.out.println("        e.g. '1 2 DATA', '1 3 ACK' etc.");
		
		System.out.println("\n  - Delay a packet:");
		System.out.println("      2 [Block #] [DATA or ACK]");
		System.out.println("        e.g. '2 2 DATA', '2 3 ACK' etc.");
		
		System.out.println("\n  - Duplicate a packet:");
		System.out.println("      3 [Block #] [DATA or ACK]");
		System.out.println("        e.g. '3 2 DATA', '3 3 ACK' etc.");
	}
	
	private void handleLosePacketInput(String input) {
		this.mode = Mode.LOSE;
		String[] parts = input.split(" ");
		this.blockNumber = Integer.parseInt(parts[1]);
		String packetTypeStr = parts[2];
		if(packetTypeStr.equals("DATA")) {
			this.packetType = PacketType.DATA;
		} else if(packetTypeStr.equals("ACK")) {
			this.packetType = PacketType.ACK;
		}
		System.out.println("\n" + packetType + " packet with block #" + blockNumber + " will be lost.\n");
	}
	
	public void takeInput() {
		Scanner in = new Scanner(System.in);
		while(true) {
			printHelp();
			System.out.print("\n> ");
			String s = in.nextLine();
			if(s.equals("0")) {
				this.mode = Mode.NORMAL;
				break;
			} else if(s.startsWith("1")) {	// lose packet
				handleLosePacketInput(s);
				break;
			}
		}
		simulateErrors();
		in.close();
	}

	public static void main(String[] args) {
		ErrorSimulator h = new ErrorSimulator();
//		h.simulateErrors();
		h.takeInput();
	}

}
