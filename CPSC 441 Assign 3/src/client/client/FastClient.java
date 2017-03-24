package client.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Timer;

import client.Queue.TxQueue;
import client.Queue.TxQueueNode;


/**
 * FastClient Class
 * 
 * FastClient implements a basic reliable FTP client application based on UDP data transmission and selective repeat protocol
 * 
 */
public class FastClient {
	private String server_name;
	private int server_port;
	private int window;
	private int timeout;
	public static final int SERVER_PORT = 5555;
	private DataOutputStream outputStream;
	private DataInputStream inputStream;
	private DataOutputStream outputStream2;
	private DataInputStream inputStream2;
	private static final int MAX_BYTE_SIZE = 1000;
	private static final int NO_DATA_RECEIVED = -1;
	private String file_name;
	private DatagramSocket clientSocket;
	private DatagramPacket sendPacket;
	private DatagramPacket receivePacket;
	private InetAddress IPAddress;
	private boolean isEnd = false;
	
	static TxQueue queue;
 	/**
        * Constructor to initialize the program 
        * 
        * @param server_name    server name or IP
        * @param server_port    server port
        * @param file_name      file to be transfered
        * @param window         window size
	* @param timeout	time out value
        */
	public FastClient(String server_name, int server_port, int window, int timeout, 
			String file_name) {
	
	/* initialize */	
		this.server_name = server_name;
		this.server_port = server_port;
		this.window = window;
		this.timeout = timeout;
		this.file_name = file_name;
	}
	
	/* send file */

	public void send(String file_name) {
		Path path = Paths.get(this.file_name);
		byte [] fileByteInfo = null;
		Socket socket = null;
		byte checkForReceivedInfo = Byte.MIN_VALUE;
		byte [] dataToSend = new byte[MAX_BYTE_SIZE];
		Segment segment = null;
		boolean segmentCheck;

		queue = new TxQueue(this.window);
		byte [] ACKCheck;
		int indexFileInfo;
		int indexSender;
		byte[][] dataPackets;
		byte[] lastDataPacket;
		int numPackets;
		int fileLength;
		int seqNum = 0;

		
		
		try {
			fileByteInfo = Files.readAllBytes(path);
			fileLength = fileByteInfo.length;
			socket = new Socket(this.server_name,SERVER_PORT);
			checkForReceivedInfo = 1;
			segment = new Segment();
			segmentCheck = true;
			clientSocket = new DatagramSocket();
			IPAddress = InetAddress.getByName("localhost");
			ACKCheck = new byte[window];

			
			outputStream = new DataOutputStream(socket.getOutputStream());
			outputStream.writeUTF(this.file_name);			
			outputStream.flush();
			inputStream = new DataInputStream(socket.getInputStream());
			checkForReceivedInfo = inputStream.readByte();				
			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
		
		
		if(checkForReceivedInfo == 0)
		{
			checkForReceivedInfo = NO_DATA_RECEIVED;
			indexFileInfo = 0;
			indexSender = 0;
			
			while(indexFileInfo < fileByteInfo.length)
			{
				if(fileByteInfo.length - indexFileInfo < 1000)
				{
					break;
				}
				while(indexSender < dataToSend.length && indexFileInfo < fileByteInfo.length && indexSender < MAX_BYTE_SIZE)
				{
					dataToSend[indexSender] = fileByteInfo[indexFileInfo];
					indexSender++;
					indexFileInfo++;
				}
				indexSender = 0;
				segment.setPayload(dataToSend);
				segment.setSeqNum(seqNum++);
				processSend(segment);
				
			}
			
		}




		
		
	}
	
	/* Move processAck processSend, and processTime to FastClient
	 * Pass FastClient to other functions
	 * Use AckHandler to get receiveData
	 * 
	 * In AckHandler, use receiveData to wait for data for the specified amount of time,
	 * and tell to resend if nothing found
	 * 
	 */
	
/*	public synchronized void processAck(Segment Ack)
	{
		byte[] ACKCheck = new byte[1];
		ACKCheck[0] = (byte) Ack.getSeqNum();
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
	}*/
	
	
	public synchronized void processSend(Segment segment)
	{
		int checkForReceivedInfo = NO_DATA_RECEIVED;
		DatagramSocket clientSocket = null;
		DatagramPacket receivePacket = null;
		byte[] ACKCheck = new byte[1];
		Timer aTimer = new Timer();
		AckHandler handler;
		
		try {
			clientSocket = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			DatagramPacket sendPacket = new DatagramPacket(segment.getBytes(), segment.getLength(),IPAddress, SERVER_PORT);
			clientSocket.send(sendPacket);
			queue.add(segment);
			handler = new AckHandler(this,segment, clientSocket);
			TimeOutHandler timeOut;
			aTimer.schedule(timeOut = new TimeOutHandler(segment,this.timeout, clientSocket, IPAddress, SERVER_PORT, this,segment.getSeqNum()), (long) this.timeout);
			startHandler(handler);
			//			timeOut.processAck();
//			timeOut.run();
			//			processAck(segment,clientSocket);
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		// add segment to the queue, send segment,
		// set the state of segment in the queue as "sent" and 
		// schedule timertask for the segment
	}
	
	public synchronized void startHandler(AckHandler handler)
	{
		handler.run();
	}
	
	public synchronized void processAck(Segment Ack)
	{
		
		queue.getNode(Ack.getSeqNum()).setStatus(TxQueueNode.ACKNOWLEDGED);
		System.out.println("Acknowledged Ack " + Ack.getSeqNum());
		
		while(queue.getHeadNode() != null && queue.getHeadNode().getStatus() == TxQueueNode.ACKNOWLEDGED)
		{
			try {
				System.out.println("Removed Ack " + queue.getHeadSegment().getSeqNum());
				queue.remove();
				
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			};
		}
		
		
		
		/*		byte[] ACKCheck = new byte[1];
		ACKCheck[0] = (byte) Ack.getSeqNum();
		byte ACKChecklength[] = new byte[1];
		DatagramPacket recievePacket = new DatagramPacket(ACKCheck,ACKCheck.length);
		try {
			clientSocket.receive(recievePacket);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		// If ack belongs to the current sender window => set the 
	// state of segment in the transmission queue as 
	// "acknowledged". Also, until an unacknowledged
	// segment is found at the head of the transmission 
	// queue, keep removing segments from the queue
	// Otherwise => ignore ack
	}
	
	public synchronized void processTime(int seqNum)
	{
		DatagramSocket clientSocket;
		DatagramPacket sendPacket;
		Timer aTimer = new Timer();
		
		System.out.println("Entered Proccess time for seqNum " + seqNum);
		
		
		
		
		if(queue.getNode(seqNum) != null && queue.getNode(seqNum).getStatus() != TxQueueNode.ACKNOWLEDGED)
		{
			System.out.println("Failed to send seqNum " + seqNum + ", resending");
			sendPacket = new DatagramPacket(queue.getSegment(seqNum).getBytes(),queue.getSegment(seqNum).getLength(),this.IPAddress,SERVER_PORT);
			try {
				clientSocket = new DatagramSocket();
				clientSocket.send(sendPacket);
				TimeOutHandler timeOut;
				aTimer.schedule(timeOut = new TimeOutHandler(queue.getSegment(seqNum),this.timeout, clientSocket, IPAddress, SERVER_PORT, this,seqNum), (long) this.timeout);
				
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
		}
		
		
	// Keeping track of timer tasks for each segment may 
	// be difficult. An easier way is to check whether the 
	// time-out happened for a segment that belongs
	// to the current window and not yet acknowledged.
	// If yes => then resend the segment and schedule 
	// timer task for the segment.
	// Otherwise => ignore the time-out event.
	}

    /**
     * A simple test driver
     * 
     */
	
	
	public boolean isEnd()
	{
		return isEnd;
	}
	
	
	public static void main(String[] args) {
		int window = 10; //segments
		int timeout = 1000; // milli-seconds (don't change this value)
		
		String server = "localhost";
		String file_name = "";
		int server_port = 0;
		
		// check for command line arguments
		if (args.length == 4) {
			// either provide 3 parameters
			server = args[0];
			server_port = Integer.parseInt(args[1]);
			file_name = args[2];
			window = Integer.parseInt(args[3]);
		}//awserf
		else {
			System.out.println("wrong number of arguments, try again.");
			System.out.println("usage: java FastClient server port file windowsize");
			System.exit(0);
		}
		
		FastClient fc = new FastClient(server, server_port, window, timeout, file_name);
		
		System.out.printf("sending file \'%s\' to server...\n", file_name);
		fc.send(file_name);
		System.out.println("file transfer completed.");
	}

}
