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
	private static final int MAX_BYTE_SIZE = 1000;
	private static final int NO_DATA_RECEIVED = -1;
	private String file_name;
	private DatagramSocket clientSocket;
	private InetAddress IPAddress;
	private boolean isEnd = false;
	private Timer aTimer;
	
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
	public FastClient(String server_name, int server_port, int window, int timeout) {
	
	/* initialize */	
		this.server_name = server_name;
		this.server_port = server_port;
		this.window = window;
		this.timeout = timeout;

	}
	
	/* send file */

	public void send(String file_name) {
		this.file_name = file_name;
		Path path = Paths.get(this.file_name);
		byte [] fileByteInfo = null;
		Socket socket = null;
		byte checkForReceivedInfo = Byte.MIN_VALUE;
		byte [] dataToSend = new byte[MAX_BYTE_SIZE];
		Segment segment = null;

		queue = new TxQueue(this.window);
		int indexFileInfo;
		int indexSender;
		int seqNum = 0;

		
		
		try {
			fileByteInfo = Files.readAllBytes(path);
			socket = new Socket(this.server_name,SERVER_PORT);
			checkForReceivedInfo = 1;
			segment = new Segment();
			clientSocket = new DatagramSocket(7777);
			AckHandler handler = new AckHandler(this, clientSocket);
			Thread aThread = new Thread(handler);
			aThread.start();
			aTimer = new Timer();
			
			
			IPAddress = InetAddress.getByName("localhost");

			
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
				dataToSend = new byte[MAX_BYTE_SIZE];
				while(indexSender < dataToSend.length && indexFileInfo < fileByteInfo.length && indexSender < MAX_BYTE_SIZE)
				{
					dataToSend[indexSender] = fileByteInfo[indexFileInfo];
					indexSender++;
					indexFileInfo++;
				}
				indexSender = 0;
				segment = new Segment();
				segment.setPayload(dataToSend);
				segment.setSeqNum(seqNum);
				seqNum++;
				while(queue.isFull()){}
				
				processSend(segment);
				
			}
			
			dataToSend = new byte[fileByteInfo.length - indexFileInfo];
			indexSender = 0;
			while(indexSender < dataToSend.length && indexFileInfo < fileByteInfo.length && indexSender < MAX_BYTE_SIZE)
			{
				dataToSend[indexSender] = fileByteInfo[indexFileInfo];
				indexSender++;
				indexFileInfo++;
			}
			
			segment = new Segment();
			segment.setPayload(dataToSend);
			segment.setSeqNum(seqNum);
			while(queue.isFull()){}
			
			processSend(segment);
			
			
			do
			{
			
				if(queue.isEmpty())
				{	
					isEnd = true;
					try {
						outputStream.writeByte(0);
						inputStream.close();
						outputStream.close();				
					} catch (IOException e) {
						// TODO Auto-generated catch block
						System.out.println(queue.isEmpty());
					}
					
					aTimer.cancel();
					clientSocket.close();
					break;
				}
			}while(true);
			
			
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
	

	
	public synchronized void processSend(Segment segment)
	{

		try {
			DatagramPacket sendPacket = new DatagramPacket(segment.getBytes(), segment.getLength(),IPAddress, SERVER_PORT);
			this.clientSocket.send(sendPacket);
			queue.add(segment);
			TimeOutHandler timeOut;
			aTimer.schedule(timeOut = new TimeOutHandler(this,segment.getSeqNum()), (long) this.timeout);

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

	
	public synchronized void processAck(Segment Ack)
	{
/*		try
		{
			queue.getNode(Ack.getSeqNum()).setStatus(TxQueueNode.ACKNOWLEDGED);
		}
		catch(NullPointerException e)
		{
			System.out.println(Ack.getSeqNum() + " already removed");
			//do nothing
		}*/
		
		
		
		
		if(queue.getNode(Ack.getSeqNum()) != null)
		{
		
			queue.getNode(Ack.getSeqNum()).setStatus(TxQueueNode.ACKNOWLEDGED);
			System.out.println("Head of queue is " + queue.getHeadSegment().getSeqNum());
			System.out.println("Acknowledged Ack " + Ack.getSeqNum());
//			waitForSend = false;
			
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
		
		}
		else
		{
//			waitForSend = false;
			System.out.println("Did not remove " + Ack.getSeqNum());
		}
	
		// If ack belongs to the current sender window => set the 
	// state of segment in the transmission queue as 
	// "acknowledged". Also, until an unacknowledged
	// segment is found at the head of the transmission 
	// queue, keep removing segments from the queue
	// Otherwise => ignore ack
	}
	
	public synchronized void processTime(int seqNum)
	{
//		DatagramSocket clientSocket;
		DatagramPacket sendPacket;
		AckHandler handler;
		
//		System.out.println("Wait for send is" + waitForSend);		
		
		if(queue.getNode(seqNum) != null && queue.getNode(seqNum).getStatus() != TxQueueNode.ACKNOWLEDGED)
		{
			System.out.println("Failed to send seqNum " + seqNum + ", resending");
			sendPacket = new DatagramPacket(queue.getSegment(seqNum).getBytes(),queue.getSegment(seqNum).getLength(),this.IPAddress,SERVER_PORT);
			try {

				this.clientSocket.send(sendPacket);
				TimeOutHandler timeOut;
//				handler = new AckHandler(this,queue.getSegment(seqNum), clientSocket);
//				aTimer.schedule(timeOut = new TimeOutHandler(queue.getSegment(seqNum),this.timeout, clientSocket, IPAddress, SERVER_PORT, this,seqNum), (long) this.timeout);
				aTimer.schedule(timeOut = new TimeOutHandler(this,seqNum), (long) this.timeout);

				//				handler.run();
				
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
		int timeout = 100; // milli-seconds (don't change this value)
		
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
		
		FastClient fc = new FastClient(server, server_port, window, timeout);
		
		System.out.printf("sending file \'%s\' to server...\n", file_name);
		fc.send(file_name);
		System.out.println("file transfer completed.");
	}

}
