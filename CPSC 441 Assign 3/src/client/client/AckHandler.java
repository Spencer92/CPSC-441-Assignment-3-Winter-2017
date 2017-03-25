package client.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class AckHandler implements Runnable
{
	/*
	 * get Ackhandler stuff
	 */
	
	FastClient client;
	private DatagramSocket clientSocket;
	
	/**
	 * This checks to see if any data has been sent from the server
	 * 
	 * 
	 * @param client FastClint
	 * @param clientSocket The socket that is used to talk to the server
	 */
	
	public AckHandler(FastClient client, DatagramSocket clientSocket)
	{
		this.client = client;
		this.clientSocket = clientSocket;
	
	}
	
	/**
	 * Until the program ends, this checks to see if there is any
	 * data that the server has sent
	 * 
	 */

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
				//clientSocket could try to still receive something
				//after the client has ended
				if(client.isEnd())
				{
					break;
				}
				else
				{
					e.printStackTrace();
				}
			}

		}
	}
}
