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
	
	public TimeOutHandler(Segment packet, long delay, DatagramSocket clientSocket,
										InetAddress IPAddress, int server_port)
	{
		this.packet = packet;
		this.delay = delay;
		this.clientSocket = clientSocket;
		this.IPAddress = IPAddress;
		this.server_port = server_port;
		System.out.println("Entered TimeOutHandler");
		processAck(packet,clientSocket);
		
	}
	
	public synchronized void processAck(Segment Ack, DatagramSocket clientSocket)
	{
		byte[] ACKCheck = new byte[1];
		ACKCheck[0] = (byte) Ack.getSeqNum();
		byte ACKChecklength[] = new byte[1];
		DatagramPacket recievePacket = new DatagramPacket(ACKCheck,ACKCheck.length);
		try {
			clientSocket.receive(recievePacket);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		int checkForReceivedInfo = NO_DATA_RECEIVED;
		DatagramSocket clientSocket = null;
		DatagramPacket receivePacket = null;
		byte[] ACKCheck = new byte[1];
		Timer aTimer = new Timer();
		
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
			aTimer.schedule(new TimeOutHandler(packet,this.delay, clientSocket, this.IPAddress, this.server_port),this.delay);
			processAck(packet,clientSocket);
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		//recieve ack
		//check if correct ack
		//if it is, remove head
		//if not, remove head and add to end of queue
		//maybe
		
		
	}

}
