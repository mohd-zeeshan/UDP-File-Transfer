package packet;

import java.net.InetAddress;

/**
 * ReadRequestPacket class inherits methods from Packet class. with opcode 1
 *
 */
public class ReadRequestPacket extends RequestPacket {

	/**
	 * Constructor for class ReadRequestPacket
	 * @param filename
	 * @param mode
	 * @param address
	 * @param port
	 */
	public ReadRequestPacket(String filename, String mode, InetAddress address, int port) {
		super(Packet.RRQ_OPCODE, filename, mode, address, port);
	}

}
