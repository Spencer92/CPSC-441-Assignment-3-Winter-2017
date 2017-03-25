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
	Segment packet = null;
	long delay = -1;
	int num = 0;
	DatagramSocket clientSocket;
	private static int NO_DATA_RECEIVED = -1;
	private InetAddress IPAddress;
	private int server_port;
	FastClient client;
	private int seqNum;
	
	public TimeOutHandler(Segment packet, long delay, DatagramSocket clientSocket,
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
		
	}
/*	
	public synchronized void processAck(/*Segment Ack, DatagramSocket clientSocket)*/
/*	{
		byte[] ACKCheck = new byte[1];
		ACKCheck[0] = (byte) packet.getSeqNum();
		byte ACKChecklength[] = new byte[1];
		boolean gotInfo = false;
		DatagramPacket recievePacket = new DatagramPacket(ACKCheck,ACKCheck.length);
		try {
			clientSocket.receive(recievePacket);
			gotInfo = true;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(gotInfo)
		{
			System.out.println("Got info");
		}
		else
		{
			System.out.println("Did not get info");
		}
		// If ack belongs to the current sender window => set the 
	// state of segment in the transmission queue as 
	// "acknowledged". Also, until an unacknowledged
	// segment is found at the head of the transmission 
	// queue, keep removing segments from the queue
	// Otherwise => ignore ack
<<<<<<< Updated upstream
	}
//asdfasdfasfd	
=======
	}*/
	
	@Override
	public void run() 
	{

		
			System.out.println("Before trying to processTime " + seqNum);
			try {
				Thread.sleep(100); //Needed to ensure that a received ack is set before the time runs out
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			client.processTime(seqNum);	

		
		//recieve ack
		//check if correct ack
		//if it is, remove head
		//if not, remove head and add to end of queue
		//maybe
		
		
	}

}
