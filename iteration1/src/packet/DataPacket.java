package packet;

import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * DataPacket class inherits methods from Packet class, has some instance and class method related to
 * a DATA packet.
 *
 */
public class DataPacket extends Packet {

	private int block;
	private byte[] data;
	
	/**
	 * Constructor for class DataPacket.
	 * @param block
	 * @param data
	 * @param address
	 * @param port
	 */
	public DataPacket(int block, byte[] data, InetAddress address, int port) {
		super(Packet.DATA_OPCODE, address, port);
		this.block = block;
		this.data = data;
		byte[] msg = getRequest();
		this.packet = new DatagramPacket(msg, msg.length, address, port);
	}
	
	/**
	 * Returns DATA in following format:
	 * 
	        2 bytes     2 bytes      n bytes
	       ----------------------------------
	      | Opcode |   Block #  |   Data     |
	       ----------------------------------
	 * 
	 * @return
	 */
	private byte[] getRequest() {
		int index = 0;
		int length = 2 + 2 + data.length;	// 2 bytes for opcode, 2 for block
		byte[] msg = new byte[length];
		byte zeroByte = 0;
		msg[index] = zeroByte;
		index++;
		msg[index] = (byte) opcode;
		index++;
		msg[index] = zeroByte;
		index++;
		msg[index] = (byte) block;
		index++;
		for(int i=0; i<data.length; i++) {
			msg[index] = data[i];
			index++;
		}
		return msg;
	}
	
	/**
	 * Extracts the data (file content)  and returns it in byte[]
	 * @param packet
	 * @return
	 */
	public static byte[] getDataFromPacket(DatagramPacket packet) {
		byte[] content = new byte[packet.getLength()-4];
		byte[] data = packet.getData();
		int index = 0;
		for(int i=4; i<packet.getLength(); i++) {
			content[index] = data[i];
			index++;
		}
		return content;
	}

	/**
	 * Checks if its the last packet being sent.
	 * @param packet
	 * @return
	 */
	public static boolean isLastPacket(DatagramPacket packet) {
		return packet.getLength() < Packet.DATA_PACKET_SIZE-4;
	}

}
