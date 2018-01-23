// Code taken from the provided Sample Code - Simple Echo Client Server

// SimpleEchoClient.java
// This class is the client side for a simple echo server based on
// UDP/IP. The client sends a character string to the echo server, then waits 
// for the server to send it back to the client.
// Last edited January 9th, 2016

import java.io.*;
import java.net.*;
import java.util.Arrays;

public class Client {

	private static final int PORT = Ports.CLIENT_PORT; 
	   private static final int DESTINATION_PORT = Ports.HOST_PORT;
   
   DatagramPacket sendPacket, receivePacket;
   DatagramSocket sendReceiveSocket;

   public Client() {}

   public void sendAndReceive() {
	  
	  for(int i=0; i<11;i++) {	
		  createSockets();
		  
	      String filename = "test.txt";
	      String mode = "octet";
	
	      byte fileByte[] = filename.getBytes();
	      byte modebyte[] = mode.getBytes();
	      byte packet[] = new byte[fileByte.length + modebyte.length + 4];
	      
	      int j = 0;
	      
	      if (i == 11) {
	    	  packet[j++] = (byte) 1;
	    	  packet[j++] = (byte) 1;
	      } else {
	    	  packet[j++] = (byte) 0;
	    	  packet[j++] = (byte) ((i % 2)+1);
	      }
	      
	      for(int k = 0; k < fileByte.length; k++) {
	    	  packet[j++] = fileByte[k];
	      }
	      
	      packet[j++] = (byte) 0;
	      
	      for(int k = 0; k < modebyte.length; k++) {
	    	  packet[j++] = modebyte[k];
	      }
	      
	      packet[j++] = (byte) 0;
	      
	      try {
	         sendPacket = new DatagramPacket(packet, packet.length,
	                                         InetAddress.getLocalHost(), DESTINATION_PORT);
	      } catch (UnknownHostException e) {
	         e.printStackTrace();
	         System.exit(1);
	      }
	      
	      printPacketDetails(sendPacket);
	      sendPacket(sendPacket);
	
	      byte data[] = new byte[4];
	      receivePacket = new DatagramPacket(data, data.length);
	      
	      receivePacket(receivePacket);
	      printPacketDetails(receivePacket);
	      	
	      closeSockets();
	      System.out.println("\n____________________\n");
	  }
   }

   private void createSockets() {
	   try {
		   sendReceiveSocket = new DatagramSocket(PORT);
	   } catch (SocketException se) {
		   se.printStackTrace();
		   System.exit(1);
	  }
   }
   
   private void closeSockets() {
	   sendReceiveSocket.close();
   }
   
   private void printPacketDetails(DatagramPacket packet) {
	  String str = new String(packet.getData());
      System.out.println("Client: Packet-String: " + str);
      System.out.println("Client: Packet-Bytes : " + Arrays.toString(packet.getData()));
   }
   
   private void sendPacket(DatagramPacket packet) {
	  try {
	     sendReceiveSocket.send(packet);
	     System.out.println("Client: Packet sent.\n");
	  } catch (IOException e) {
	     e.printStackTrace();
	     System.exit(1);
	  }
   }
   
   private void receivePacket(DatagramPacket packet) {
	  try { 
         sendReceiveSocket.receive(packet);
         System.out.println("Client: Packet received:\n");
      } catch(IOException e) {
         e.printStackTrace();
         System.exit(1);
      }
   }
   
   public static void main(String args[]) {
	  System.out.println("Running...");
      Client c = new Client();
      c.sendAndReceive();
   }
}
