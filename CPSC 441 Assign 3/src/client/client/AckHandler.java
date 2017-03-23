package client.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class AckHandler extends Thread
{
	/*
	 * get Ackhandler stuff
	 */
	
	FastClient client;
	private Segment packet;
	private long delay;
	private DatagramSocket clientSocket;
	private InetAddress IPAddress;
	private int server_port;
	
	public AckHandler(Segment packet, long delay, DatagramSocket clientSocket,
			InetAddress IPAddress, int server_port, FastClient client)
	{
		this.packet = packet;
		this.delay = delay;
		this.clientSocket = clientSocket;
		this.IPAddress = IPAddress;
		this.server_port = server_port;
		this.client = client;
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
				clientSocket.receive(recievePacket);
				client.processAck(packet);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
}
