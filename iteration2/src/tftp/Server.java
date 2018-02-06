package tftp;

import java.net.*;
import java.util.Scanner;

import packet.*;

/**
 * Implementation of the Server. Receives packets from host and sends packet back to it.
 * 
 * @author Dhrubo
 *
 */
public class Server {
	
	private DatagramSocket receiveSocket;
	private DatagramPacket receivePacket;
	public static final int SERVER_PORT = 69;
	public static final String SERVER_PATH = "test_files/server/";
	
	/**
	 * Constructor for class Server. Creates one socket for receiving packet.
	 */
	public Server() {
		try {
			receiveSocket = new DatagramSocket(Server.SERVER_PORT);
		} catch (SocketException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Waits to receive packet from host. Creates a ClientConnection thread which handles requests.
	 * 
	 */
	public void receiveAndSend() {
		Scanner in = new Scanner(System.in);
		while(true) {
			byte data[] = new byte[Packet.DATA_PACKET_SIZE];
		    receivePacket = new DatagramPacket(data, data.length);
		    ClientConnection.receive(receiveSocket, receivePacket);
		    ClientConnection connection = new ClientConnection(receivePacket);
		    Thread t = new Thread(connection);
		    t.start();
			String s = in.nextLine();
			if(s.equals("exit")) {
				System.out.println("Shutting down server...");
				break;
			}
		}
		in.close();
		System.out.println("Done!");
	}

	public static void main(String[] args) {
		Server s = new Server();
		s.receiveAndSend();
	}

}
