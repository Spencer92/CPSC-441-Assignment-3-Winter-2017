package client.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class AckHandler implements Runnable
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
	
//asdfasdf

	@Override
	public void run()
	{
		System.out.println("Entered Run for AckHandler");
		while(!client.isEnd())
		{
/*			try {
				clientSocket = new DatagramSocket();
			} catch (SocketException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}*/
			byte[] ACKCheck = new byte[Segment.MAX_SEGMENT_SIZE];
//			byte ACKChecklength[] = new byte[Segment.MAX_SEGMENT_SIZE];
			DatagramPacket recievePacket = new DatagramPacket(ACKCheck,ACKCheck.length);
			try {
//				System.out.println("Attempting to recieve packet "  + packet.getSeqNum());
				clientSocket.receive(recievePacket);
				Segment recieveSeg = new Segment(recievePacket);
				System.out.println("Received packet " + recieveSeg.getSeqNum());
				
				client.processAck(recieveSeg);
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
}
