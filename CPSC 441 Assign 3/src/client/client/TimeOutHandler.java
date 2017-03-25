package client.client;

import java.util.TimerTask;


public class TimeOutHandler extends TimerTask
{

	FastClient client;
	private int seqNum;
	
	
	/**
	 * This function occurs after a set amount of time
	 * and calls processTime in FastClient
	 * 
	 * 
	 * @param client FastClient
	 * @param seqNum The sequence number
	 */
	
	public TimeOutHandler(FastClient client, int seqNum)
	{
		this.client = client;
		this.seqNum = seqNum;
	}
	
	@Override
	public void run() 
	{
		
			client.processTime(seqNum);	

		
		
	}

}
