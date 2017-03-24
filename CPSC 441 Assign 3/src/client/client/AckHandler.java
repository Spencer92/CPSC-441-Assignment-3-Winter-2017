package client.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class AckHandler extends Thread
{
	/*
	 * get Ackhandler stuff
	 */
	
	FastClient client;
	private Segment packet;
//	private long delay;
	private DatagramSocket clientSocket;
//	private InetAddress IPAddress;
//	private int server_port;
	
	public AckHandler(FastClient client, Segment packet, DatagramSocket clientSocket)
	{
//		this.clientSocket = clientSocket;
		this.client = client;
		this.packet = packet;
		this.clientSocket = clientSocket;
		System.out.println("Entered TimeOutHandler");
		
		//processAck(packet,clientSocket);
	
	}
	


	@Override
	public void run()
	{
		while(!client.isEnd())
		{
			byte[] ACKCheck = new byte[1];
			ACKCheck[0] = (byte) packet.getSeqNum();
			byte ACKChecklength[] = new byte[1];
			DatagramPacket recievePacket = new DatagramPacket(ACKCheck,ACKCheck.length);
			try {
				System.out.println("Attempting to recieve packet " + packet.getSeqNum());
				clientSocket.receive(recievePacket);
				System.out.println("Received packet " + packet.getSeqNum());
				client.processAck(packet);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
}
