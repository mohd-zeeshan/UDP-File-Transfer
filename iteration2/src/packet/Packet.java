package packet;

import java.net.*;
import java.util.Arrays;

/**
 * Helper class that have some class methods that related to packet being sent or received
 *
 */
public class Packet {

	protected int opcode;
	private InetAddress address;
	private int port;
	protected DatagramPacket packet;
	
	public static final int RRQ_OPCODE = 1;
	public static final int WRQ_OPCODE = 2;
	public static final int DATA_OPCODE = 3;
	public static final int ACK_OPCODE = 4;
	public static final int DATA_PACKET_SIZE = 516;

	/**
	 * Constructor for class Packet
	 * @param opcode
	 * @param address
	 * @param port
	 */
	public Packet(int opcode, InetAddress address, int port) {
		this.opcode = opcode;
		this.address = address;
		this.port = port;
	}
	
	/**
	 * Getter for field address
	 * @return	Address
	 */
	public InetAddress getAddress() {
		return address;
	}

	/**
	 * Getter for field port
	 * @return	Port
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Setter for field 'packet'
	 * @param packet
	 */
	public void setPacket(DatagramPacket packet) {
		this.packet = packet;
	}

	/**
	 * Getter for field 'packet'
	 * @return	packet
	 */
	public DatagramPacket getPacket() {
		return packet;
	}
	
	/**
	 * Checks if packet is a read request.
	 * @param packet
	 * @return
	 */
	public static boolean isReadRequest(DatagramPacket packet) {
		return packet.getData()[1] == RRQ_OPCODE;
	}
	
	/**
	 * Checks if packet is a write request.
	 * @param packet
	 * @return
	 */
	public static boolean isWriteRequest(DatagramPacket packet) {
		return packet.getData()[1] == WRQ_OPCODE;
	}
	
	/**
	 * Checks if packet is an ACK.
	 * @param packet
	 * @return
	 */
	public static boolean isACK(DatagramPacket packet) {
		return packet.getData()[1] == ACK_OPCODE;
	}
	
	/**
	 * Checks if packet is DATA.
	 * @param packet
	 * @return
	 */
	public static boolean isDATA(DatagramPacket packet) {
		return packet.getData()[1] == DATA_OPCODE;
	}

	/**
	 * Prints necessary info of the packet
	 * @param packet
	 */
	public static void printRequest(DatagramPacket packet) {
	    System.out.println("address: " + packet.getAddress());
		System.out.println("port: " + packet.getPort());
		int len = packet.getLength();
		byte[] data = packet.getData();
		String msg = new String(data,0,len);
		System.out.println("Length: " + len);
		System.out.println("Request (string): " + msg);
	    System.out.println("Request (byte array): " + Arrays.toString(msg.getBytes()));
	}
	
	/**
	 * Get block number in byte[] from the packet.
	 * @param packet
	 * @return
	 */
	public static byte[] getBlock(DatagramPacket packet) {
		byte[] data = packet.getData();
		byte[] blockNum = {data[2], data[3]};
		return blockNum;
	}
	
	/**
	 * Gets block number in int from packet.
	 * @param packet
	 * @return
	 */
	public static int getBlockNumber(DatagramPacket packet) {
		return packet.getData()[3];
	}

}
