package packet;

import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * ACKPacket class inherits methods from Packet class, has some instance and class method related to
 * an ACK packet.
 *
 */
public class ACKPacket extends Packet {
	
	private byte[] block;

	/**
	 * Constructor for class ACKPacket
	 * @param block
	 * @param address
	 * @param port
	 */
	public ACKPacket(byte[] block, InetAddress address, int port) {
		super(Packet.ACK_OPCODE, address, port);
		this.block = block;
		byte[] msg = getRequest();
		this.packet = new DatagramPacket(msg, msg.length, address, port);
	}

	/**
	 * Returns ACK content in following format (in byte[]):
	 * 
		     2 bytes     2 bytes
			 ---------------------
			| Opcode |   Block #  |
			 ---------------------
			 
	 * @return
	 */
	private byte[] getRequest() {
		byte[] msg = new byte[4];
		int index = 0;
		msg[index] = 0;
		index++;
		msg[index] = (byte) this.opcode;
		index++;
		msg[index] = block[0];
		index++;
		msg[index] = block[1];
		return msg;
	}
	
	/**
	 * Takes an integer and returns block number in byte[]
	 * @param blockNum
	 * @return
	 */
	public static byte[] getBlockFromInt(int blockNum) {
		byte[] blk = new byte[] {0, (byte)blockNum};
		return blk;
	}

}
