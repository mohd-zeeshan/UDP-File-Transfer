package packet;

import java.net.DatagramPacket;
import java.net.InetAddress;

public class ErrorPacket extends Packet {
	private int errCode;
	
	public ErrorPacket(int errCode, byte[] errMsg, InetAddress address, int port) {
		super(Packet.ERROR_OPCODE, address, port);
		this.errCode = errCode;
		byte[] msg = getErrorMsg(errMsg);
		this.packet = new DatagramPacket(msg, msg.length, address, port);
	}
	
	public byte[] getErrorMsg(byte[] errMsg) {
		int index = 0;
		int length = 2 + 2 + errMsg.length + 1;
		byte msg[] = new byte[length];
		byte zeroByte = 0;
		msg[index] = zeroByte;
		index++;
		msg[index] = (byte)opcode;
		index++;
		msg[index] = zeroByte;
		index++;
		msg[index] = (byte) errCode;
		index++;
		for(int i=0; i<errMsg.length; i++) {
			msg[index] = errMsg[i];
			index++;
		}
		msg[index] = zeroByte;
		return msg;
	}
	
}
