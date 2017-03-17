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

import udp.segment.Segment;

/**
 * FastClient Class
 * 
 * FastClient implements a basic reliable FTP client application based on UDP data transmission and selective repeat protocol
 * 
 */
public class FastClient {
	private String server_name;
	private int server_port;
	private String file_name;
	private int timeout;
	private static final int SERVER_PORT = 5555;
	private DataOutputStream outputStream;
	private DataInputStream inputStream;
	private DataOutputStream outputStream2;
	private DataInputStream inputStream2;
	private static final int MAX_BYTE_SIZE = 1000;
	private static final int NO_DATA_RECEIVED = -1;
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
		this.file_name = file_name;
		this.timeout = timeout;
	}
	
	/* send file */

	public void send(String file_name) {
		Path path = Paths.get(this.file_name);
		try {
			byte [] fileByteInfo = Files.readAllBytes(path);
			Socket socket = new Socket(this.server_name,SERVER_PORT);
			byte checkForReceivedInfo = 1;
			byte [] dataToSend = new byte[MAX_BYTE_SIZE];
			Segment segment = new Segment();
			boolean segmentCheck = true;
			DatagramSocket clientSocket = new DatagramSocket();
			DatagramPacket sendPacket;
			DatagramPacket receivePacket;
			InetAddress IPAddress = InetAddress.getByName("localhost");
			byte [] ACKCheck = new byte[1];
			int indexFileInfo;
			int indexSender;
			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}



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
