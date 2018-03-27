package packet;

import java.net.DatagramPacket;
import java.net.InetAddress;

/**
 * RequestPacket class inherits methods from Packet class, has some instance and class method related to
 * an RRQ/WRQ.
 *
 */
public class RequestPacket extends Packet {
	
	private String filename;
	private String mode;
	
	/**
	 * Constructor for class RequestPacket
	 * @param opcode
	 * @param filename
	 * @param mode
	 * @param address
	 * @param port
	 */
	public RequestPacket(int opcode, String filename, String mode, InetAddress address, int port) {
		super(opcode, address, port);
		this.filename = filename;
		this.mode = mode;
		byte[] msg = getRequest();
		this.packet = new DatagramPacket(msg, msg.length, address, port);
	}
	
	/**
	 * Returns RRQ or WRQ in right format.
	 * @return
	 */
	private byte[] getRequest() {
		byte zeroByte = 0;
		int length = 2 + filename.length() + 1 + mode.length() + 1;
		byte data[] = new byte[length];
		int index = 0;
		data[index] = zeroByte;
		index++;
		data[index] = (byte)opcode;
		index++;
		for(int i=0; i<filename.length(); i++) {
			data[index] = (byte)filename.charAt(i);
			index++;
		}
		data[index] = zeroByte;
		index++;
		for(int i=0; i<mode.length(); i++) {
			data[index] = (byte)mode.charAt(i);
			index++;
		}
		data[index] = zeroByte;
		return data;
	}

	/**
	 * Extracts the name of the file in string from packet and returns it
	 * @param packet
	 * @return
	 */
	public static String getFilename(DatagramPacket packet) {
		int i = 2;
		String fileName = "";
		byte[] data = packet.getData();
		while(data[i] != 0) {
			fileName += Character.toString ((char) data[i]);
			i++;
		}
		return fileName;
	}
	
	/**
	 * Extracts the mode from packet and returns it
	 * @param packet
	 * @return
	 */
	public static String getMode(DatagramPacket packet) {
		int i = 2;
		byte[] data = packet.getData();
		while(data[i] != 0) {
			i++;
		}
		String mode = "";
		for(int j=i+1; j<packet.getLength()-1; j++) {
			mode += Character.toString ((char) data[j]);
		}
		return mode;
	}
	
}
