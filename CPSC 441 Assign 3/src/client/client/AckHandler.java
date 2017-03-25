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
	private DatagramSocket clientSocket;
	
	public AckHandler(FastClient client, DatagramSocket clientSocket)
	{
		this.client = client;
		this.clientSocket = clientSocket;
	
	}
	
//asdfasdf

	@Override
	public void run()
	{
		while(!client.isEnd())
		{
			byte[] ACKCheck = new byte[Segment.MAX_SEGMENT_SIZE];
			DatagramPacket recievePacket = new DatagramPacket(ACKCheck,ACKCheck.length);
			try {
				clientSocket.receive(recievePacket);
				Segment recieveSeg = new Segment(recievePacket);
				
				client.processAck(recieveSeg);
				
			} catch (IOException e) {
				if(client.isEnd())
				{
					break;
				}
				else
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
	}
}
