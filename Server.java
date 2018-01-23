// Code taken from the provided Sample Code - Simple Echo Client Server

// SimpleEchoServer.java
// This class is the server side of a simple echo server based on
// UDP/IP. The server receives from a client a packet containing a character
// string, then echoes the string back to the client.
// Last edited January 9th, 2016

import java.io.*;
import java.net.*;
import java.util.Arrays;

public class Server {

   private static final int PORT = Ports.SERVER_PORT; 
   private static final int DESTINATION_PORT = Ports.HOST_PORT;
   
   DatagramPacket sendPacket, receivePacket;
   DatagramSocket sendSocket, receiveSocket;

   public Server() {}

   public void receiveAndEcho() {
	   while (true) {
		  createSockets();
	      
		  byte data[] = new byte[17];
	      receivePacket = new DatagramPacket(data, data.length);
	      
	      receivePacket(receivePacket);
	      
	      try {
		      if(!valid(data)) throw new Exception ("Error in Packet: ");
	      }catch(Exception exception){
	    	    System.out.println(exception + "Invalid Data. Shutting down server");  
	    	    exception.printStackTrace();
		        System.exit(1);
	      }
	      
	      printPacketDetails(receivePacket);
	      
	      try {
	          Thread.sleep(5000);
	      } catch (InterruptedException e ) {
	          e.printStackTrace();
	          System.exit(1);
	      }
	      
	      data = new byte[4];
	      if(data[1] == (byte) 1) {
	    	  data[0] = (byte) 0;
	    	  data[1] = (byte) 3;
	    	  data[2] = (byte) 0;
	    	  data[3] = (byte) 1;
	      } else {
	    	  data[0] = (byte) 0;
	    	  data[1] = (byte) 4;
	    	  data[2] = (byte) 0;
	    	  data[3] = (byte) 0;
	      }
	      
	      sendPacket = new DatagramPacket(data, data.length,
	                               receivePacket.getAddress(), DESTINATION_PORT);
	
	      printPacketDetails(sendPacket);
	      sendPacket(sendPacket);
	
	      closeSockets();
	   }
   }

   private boolean valid(byte[] data) {
	   
	   if(data[0] != (byte) 0) {
		   return false;
	   }
	   if(data[1] != (byte) 1 && data[1] != (byte) 2) {
		   return false;
	   }
	   int counter = 0;
	   for(int i=2; i<data.length;i++) {
		   if(data[i] < (byte) 65 && data[i] != (byte) 46 && data[i] != (byte) 0) {
			   return false;
		   }
		   if(data[i] == (byte) 0) {
			   counter++;
		   }
		   if(counter == 2 && i<data.length-1) {
			   return false;
		   }
	   }
	   if(counter != 2) {
		   return false;
	   }
	   
	   return true;
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
      System.out.println("Server: Packet-String: " + str);
      System.out.println("Server: Packet-Bytes : " + Arrays.toString(packet.getData()));
   }
   
   private void sendPacket(DatagramPacket packet) {
	   System.out.println("Server: Sending Packet");
	  try {
         sendSocket.send(sendPacket);
         System.out.println("Server: Packet sent");
      } catch (IOException e) {
         e.printStackTrace();
         System.exit(1);
      }
   }
   
   private void receivePacket(DatagramPacket packet) {
	  System.out.println("Server: Waiting for Packet.\n");
	
      try {        
         System.out.println("Server: Waiting...");
         receiveSocket.receive(packet);
      } catch (IOException e) {
         System.out.print("Server: IO Exception: likely:");
         System.out.println("Server: Receive Socket Timed Out.\n" + e);
         e.printStackTrace();
         System.exit(1);
      }

      System.out.println("Server: Packet received:");
   }
   
   public static void main( String args[] ) {
      Server c = new Server();
      c.receiveAndEcho();
   }
}

