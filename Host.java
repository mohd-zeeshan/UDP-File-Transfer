import java.io.*;
import java.net.*;
import java.util.Arrays;

public class Host {

	private static final int PORT = Ports.HOST_PORT; 
	private static final int DESTINATION_PORT = Ports.SERVER_PORT;
   
   DatagramPacket sendPacket, receivePacket;
   DatagramSocket sendSocket, receiveSocket;

   public Host() {}

   public void receiveAndEcho() {
	   
	   while(true) {
		  createSockets();
		  
	      byte data[] = new byte[17];
	      receivePacket = new DatagramPacket(data, data.length);
	      
	      System.out.println("Host: Packet from Client:");
	      receivePacket(receivePacket);
	      printPacketDetails(receivePacket);

	      try {
	          Thread.sleep(5000);
	      } catch (InterruptedException e ) {
	          e.printStackTrace();
	          System.exit(1);
	      }
	
	      sendPacket = new DatagramPacket(data, receivePacket.getLength(),
	                               receivePacket.getAddress(), DESTINATION_PORT);
	
	      System.out.println("Host: Sending Packet to server");
	      sendPacket(sendPacket);
	      	      
	      byte recievedData[] = new byte[4];
	      receivePacket = new DatagramPacket(recievedData, recievedData.length);
	
	      receivePacket(receivePacket);
	      System.out.println("Host: Packet from Server:");
	      printPacketDetails(receivePacket);

	      sendPacket = new DatagramPacket(data, receivePacket.getLength(),
	                               receivePacket.getAddress(), 30);
	
	      System.out.println("Host: Sending Packet to Client");
	      sendPacket(sendPacket);
	      
	      System.out.println("Host: packet sent");
	      closeSockets();
	   }
   }

   
   private void createSockets() {
	  try {
         sendSocket = new DatagramSocket();
         receiveSocket = new DatagramSocket(PORT);
      } catch (SocketException se) {
         se.printStackTrace();
         System.exit(1);
      } 
   }
   
   private void closeSockets() {
	  sendSocket.close();
	  receiveSocket.close();
   }
   
   private void printPacketDetails(DatagramPacket packet) {
	  String str = new String(packet.getData());
      System.out.println("Host: Packet-String: " + str);
      System.out.println("Host: Packet-Bytes : " + Arrays.toString(packet.getData()));
   }
   
   private void sendPacket(DatagramPacket packet) {
	  System.out.println("Host: Sending Packet");
	  try {
         sendSocket.send(sendPacket);
         System.out.println("Host: Packet sent");
      } catch (IOException e) {
         e.printStackTrace();
         System.exit(1);
      }
   }
   
   private void receivePacket(DatagramPacket packet) {
	  System.out.println("Host: Waiting for Packet.\n");
	
      try {        
         System.out.println("Host: Waiting...");
         receiveSocket.receive(packet);
      } catch (IOException e) {
         System.out.print("Host: IO Exception: likely:");
         System.out.println("Host: Receive Socket Timed Out.\n" + e);
         e.printStackTrace();
         System.exit(1);
      }

      System.out.println("Host: Packet received:");
   }
   
   public static void main( String args[] ) {
      Host c = new Host();
      c.receiveAndEcho();
   }
}