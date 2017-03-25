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
	
	/**
	 * This reads the file, breaks it up into chunks, and
	 * sends out the chunks for processesing, in order
	 * for the chunks to be sent to the server
	 * 
	 * @param file_name
	 */

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

			//Start the thread that will receive the acks
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
				
				//Move the data into 1000 byte chunks for sending
				while(indexSender < dataToSend.length && indexFileInfo < fileByteInfo.length && indexSender < MAX_BYTE_SIZE)
				{
					dataToSend[indexSender] = fileByteInfo[indexFileInfo];
					indexSender++;
					indexFileInfo++;
				}
				indexSender = 0;
				
				//Create a segment with the data
				segment = new Segment();
				segment.setPayload(dataToSend);
				segment.setSeqNum(seqNum);
				seqNum++;
				
				//Wait for a spot to open on the queue
				while(queue.isFull()){}
				
				processSend(segment);
				
			}
			
			
			//If the file isn't a multiple of 1000, there will be some
			//data left that needs to be received
			if(fileByteInfo.length - indexFileInfo != 0)
			{
				dataToSend = new byte[fileByteInfo.length - indexFileInfo];
				indexSender = 0;
				
				//move the data into 1000 byte chunks for sending
				while(indexSender < dataToSend.length && indexFileInfo < fileByteInfo.length && indexSender < MAX_BYTE_SIZE)
				{
					dataToSend[indexSender] = fileByteInfo[indexFileInfo];
					indexSender++;
					indexFileInfo++;
				}
				//create a segment with the data
				segment = new Segment();
				segment.setPayload(dataToSend);
				segment.setSeqNum(seqNum);
				
				//wait for there to be a spot on the queue
				while(queue.isFull()){}
				
				processSend(segment);
			}
			
			do
			{
			//Don't end the program until the queue has been completely emptied
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
				}//if queue.isEmpty()
			}while(true);
			
			
		}




		
		
	}
	
	
	/**
	 * 
	 * This prepares the data to be sent to the server,
	 * it is then added to the queue in order to check when the server
	 * gives an ack
	 * 
	 * @param segment The data to be processed
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
		
	}

	/**
	 *	Once an Ack has been received, it needs to be processed
	 *	in order to see if the Ack is a duplicate or not
	 *
	 *	If it is a duplicate, the Ack is ignored, 
	 *	otherwise, it states that the ack has been received
	 *	and is removed from the queue if possible at that time
	 *
	 *
	 * @param Ack the segment that needs to be checked
	 */
	
	
	public synchronized void processAck(Segment Ack)
	{		
		
		if(queue.getNode(Ack.getSeqNum()) != null)
		{
			//If the node is in the queue, state that it has been acknowledged
			queue.getNode(Ack.getSeqNum()).setStatus(TxQueueNode.ACKNOWLEDGED);			
			while(queue.getHeadNode() != null && queue.getHeadNode().getStatus() == TxQueueNode.ACKNOWLEDGED)
			{
				try {
					queue.remove();
					
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				};
			}
		
		}
	
	}
	
	/**
	 * 
	 * When a certain amount of time has passed, this function
	 * checks to see if the Ack was received, and resends the data
	 * if it turns out it wasn't sent
	 * 
	 * @param seqNum The data in the queue that needs to be checked if
	 * it was acked
	 */
	
	
	public synchronized void processTime(int seqNum)
	{
		DatagramPacket sendPacket;
			
		
		if(queue.getNode(seqNum) != null && queue.getNode(seqNum).getStatus() != TxQueueNode.ACKNOWLEDGED)
		{
			//If the packet failed to send, resend packet
			sendPacket = new DatagramPacket(queue.getSegment(seqNum).getBytes(),queue.getSegment(seqNum).getLength(),this.IPAddress,SERVER_PORT);
			try {

				this.clientSocket.send(sendPacket);
				TimeOutHandler timeOut;
				aTimer.schedule(timeOut = new TimeOutHandler(this,seqNum), (long) this.timeout);

				
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
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
