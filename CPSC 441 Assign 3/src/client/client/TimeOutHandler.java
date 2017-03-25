package client.client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Timer;
import java.util.TimerTask;

import client.Queue.TxQueue;
import client.Queue.TxQueueNode;

public class TimeOutHandler extends TimerTask
{
//	Segment packet = null;
//	long delay = -1;
//	int num = 0;
//	DatagramSocket clientSocket;
//	private static int NO_DATA_RECEIVED = -1;
//	private InetAddress IPAddress;
//	private int server_port;
	FastClient client;
	private int seqNum;
	
/*	public TimeOutHandler(Segment packet, long delay, DatagramSocket clientSocket,
										InetAddress IPAddress, int server_port, FastClient client,
										int seqNum)
	{
		this.packet = packet;
		this.delay = delay;
		this.clientSocket = clientSocket;
		this.IPAddress = IPAddress;
		this.server_port = server_port;
		this.client = client;
		System.out.println("Entered TimeOutHandler");
		this.seqNum = seqNum;
		
		
//		processAck(packet,clientSocket);
		
	}*/
	
	public TimeOutHandler(FastClient client, int seqNum)
	{
		this.client = client;
		this.seqNum = seqNum;
	}
	
	@Override
	public void run() 
	{
			try {
				Thread.sleep(100); //Needed to ensure that a received ack is set before the time runs out
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			client.processTime(seqNum);	

		
		
	}

}
