package packet;

import java.net.InetAddress;

/**
 * WriteRequestPacket class inherits methods from Packet class, with opcode=2
 *
 */
public class WriteRequestPacket extends RequestPacket {
	/**
	 * Constructor for class WriteRequestPacket
	 * @param filename
	 * @param mode
	 * @param address
	 * @param port
	 */
	public WriteRequestPacket(String filename, String mode, InetAddress address, int port) {
		super(Packet.WRQ_OPCODE, filename, mode, address, port);
	}

}
