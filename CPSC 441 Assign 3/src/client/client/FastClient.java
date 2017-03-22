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

import client.Queue.TxQueue;


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
	private static final int SERVER_PORT = 5555;
	private DataOutputStream outputStream;
	private DataInputStream inputStream;
	private DataOutputStream outputStream2;
	private DataInputStream inputStream2;
	private static final int MAX_BYTE_SIZE = 1000;
	private static final int NO_DATA_RECEIVED = -1;
	private String file_name = "file100KB";
	private DatagramSocket clientSocket;
	private DatagramPacket sendPacket;
	private DatagramPacket receivePacket;
	private InetAddress IPAddress;
	
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
		Path path = Paths.get(this.file_name);
		byte [] fileByteInfo = null;
		Socket socket = null;
		byte checkForReceivedInfo = Byte.MIN_VALUE;
		byte [] dataToSend = new byte[MAX_BYTE_SIZE];
		Segment segment = null;
		boolean segmentCheck;


		byte [] ACKCheck;
		int indexFileInfo;
		int indexSender;
		byte[][] dataPackets;
		byte[] lastDataPacket;
		int numPackets;
		int fileLength = fileByteInfo.length;
		int seqNum = 0;
		
		try {
			fileByteInfo = Files.readAllBytes(path);
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
				processSend(segment, checkForReceivedInfo);
				
			}
			
		}




		
		
	}
	
	public synchronized void processSend(Segment segment, int checkForReceivedInfo)
	{
		checkForReceivedInfo = NO_DATA_RECEIVED;
		DatagramSocket clientSocket = null;
		DatagramPacket receivePacket = null;
		byte[] ACKCheck = new byte[1];
		try {
			clientSocket = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		DatagramPacket sendPacket = new DatagramPacket(segment.getBytes(), segment.getLength(),IPAddress, SERVER_PORT);
		try {
			clientSocket.send(sendPacket);
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// add segment to the queue, send segment,
		// set the state of segment in the queue as "sent" and 
		// schedule timertask for the segment
	}
	
	
	public synchronized void processAck(Segment Ack)
	{
		// If ack belongs to the current sender window => set the 
	// state of segment in the transmission queue as 
	// "acknowledged". Also, until an unacknowledged
	// segment is found at the head of the transmission 
	// queue, keep removing segments from the queue
	// Otherwise => ignore ack
	}
	
	public synchronized void processTime(int seqNum)
	{
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
		}
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
