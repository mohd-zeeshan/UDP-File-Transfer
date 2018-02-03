package tftp;

import java.io.IOException;
import java.net.*;

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
	
	/**
	 * Constructor for Host class. Creates a socket for receiving, another socket for both
	 * sending and receiving.
	 * 
	 */
	public ErrorSimulator() {
		try {
			receiveSocket = new DatagramSocket(Client.CLIENT_PORT);
			sendReceiveSocket = new DatagramSocket();
		} catch (SocketException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Waits to receive a request and prints out the information it has received
	 * from the client.
	 * 
	 */
	private void receiveFromClient() {
	    try {
			byte data[] = new byte[Packet.DATA_PACKET_SIZE];
		    receiveFromClientPacket = new DatagramPacket(data, data.length);
		    System.out.println("ErrorSimulator says: Waiting for Packet from client...");
	    	receiveSocket.receive(receiveFromClientPacket);
			clientPort = receiveFromClientPacket.getPort();
			clientAddress = receiveFromClientPacket.getAddress();
			RequestPacket.printRequest(receiveFromClientPacket);
			sendToServer(receiveFromClientPacket);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Forms a packet to send containing exactly what it received, sends it to server
	 * and prints the info.
	 * 
	 * @param packet	byte[] received from client
	 */
	private void sendToServer(DatagramPacket packet) {
		try {
			sendToServerPacket = new DatagramPacket(packet.getData(), packet.getLength(), InetAddress.getLocalHost(), Server.SERVER_PORT);
			System.out.println("ErrorSimulator says: Sending packet to server...");
			RequestPacket.printRequest(sendToServerPacket);
			sendReceiveSocket.send(sendToServerPacket);
		    System.out.println("Host: Packet sent.\n");
		    receiveFromServer();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
	        System.exit(1);
	    }
	}
	
	/**
	 * Waits to receive a request and prints out the information it has received from server.
	 */
	private void receiveFromServer() {
	    try {
			byte data[] = new byte[Packet.DATA_PACKET_SIZE];
		    receiveFromServerPacket = new DatagramPacket(data, data.length, InetAddress.getLocalHost(), Server.SERVER_PORT);
		    System.out.println("ErrorSimulator says: Waiting for Packet from server...");
	    	sendReceiveSocket.receive(receiveFromServerPacket);
		    RequestPacket.printRequest(receiveFromServerPacket);
		    System.out.println("Packet Recieved!\n");
		    sendToClient(receiveFromServerPacket);
    	} catch (IOException e) {
	    	e.printStackTrace();
	    	System.exit(1);
    	}
	}
	
	/**
	 * Forms a packet to send containing exactly what it received, sends it back to client
	 * and prints the info.
	 * 
	 * @param packet		Packet received from server
	 */
	private void sendToClient(DatagramPacket packet) {
		try {
			sendToClientPacket = new DatagramPacket(packet.getData(), packet.getLength(), clientAddress, clientPort);	
			System.out.println("ErrorSimulator says: Sending packet to client...");
			RequestPacket.printRequest(sendToClientPacket);
			sendReceiveSocket.send(sendToClientPacket);
			System.out.println("Host: Packet sent!\n");
		} catch (SocketException e) {
			e.printStackTrace();
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Loops infinitely and calls receiveFromClient Method.
	 */
	public void receiveAndSend() {
		while(true) {
			receiveFromClient();
		}
	}

	public static void main(String[] args) {
		ErrorSimulator h = new ErrorSimulator();
		h.receiveAndSend();
	}

}
