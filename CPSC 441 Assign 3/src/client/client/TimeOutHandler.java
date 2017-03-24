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
	
	public synchronized void processAck(/*Segment Ack, DatagramSocket clientSocket*/)
	{
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
	}
	
	@Override
	public void run() 
	{
		System.out.println("Waited too long, entering run");
/*		int checkForReceivedInfo = NO_DATA_RECEIVED;
		DatagramSocket clientSocket = null;
		DatagramPacket receivePacket = null;
		byte[] ACKCheck = new byte[1];
		Timer aTimer = new Timer();*/
	
//		DatagramSocket clientSocket;
		DatagramPacket sendPacket;
		Timer aTimer = new Timer();
		
		System.out.println("Entered Proccess time for seqNum " + seqNum);
		
		
//		System.out.println(FastClient.queue.getNode(seqNum).getStatus() == TxQueueNode.ACKNOWLEDGED);
		
		if(FastClient.queue.getNode(seqNum) != null && 
				FastClient.queue.getNode(seqNum).getStatus() != TxQueueNode.ACKNOWLEDGED)
		{
			System.out.println("Failed to send seqNum " + seqNum + ", resending");
			sendPacket = new DatagramPacket(FastClient.queue.getSegment(seqNum).getBytes(),FastClient.queue.getSegment(seqNum).getLength(),this.IPAddress,FastClient.SERVER_PORT);
			try {
				clientSocket = new DatagramSocket();
				clientSocket.send(sendPacket);
				TimeOutHandler timeOut;
				AckHandler handler = new AckHandler(client,FastClient.queue.getSegment(seqNum),clientSocket);
				aTimer.schedule(timeOut = new TimeOutHandler(FastClient.queue.getSegment(seqNum),this.delay, clientSocket, IPAddress, FastClient.SERVER_PORT, client, this.seqNum), (long) this.delay);
				handler.run();
				
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
//			processSend(queue.getSegment(seqNum));
		}
		else
		{
			System.out.println("Did not fail to send seqNum " + seqNum);
			return;
		}		
		
		
//		client.processTime(packet.getSeqNum());
		
/*		
		try {
			clientSocket = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			FastClient.queue.add(packet);
			DatagramPacket sendPacket = new DatagramPacket(packet.getBytes(), packet.getLength(),this.IPAddress, this.server_port);
			clientSocket.send(sendPacket);
			TimeOutHandler timeOut;
			aTimer.schedule(timeOut = new TimeOutHandler(packet,this.delay, clientSocket, this.IPAddress, this.server_port, this.client),this.delay);
			timeOut.processAck();
			timeOut.run();
//			processAck(packet,clientSocket);
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		*/
		
		//recieve ack
		//check if correct ack
		//if it is, remove head
		//if not, remove head and add to end of queue
		//maybe
		
		
	}

}
