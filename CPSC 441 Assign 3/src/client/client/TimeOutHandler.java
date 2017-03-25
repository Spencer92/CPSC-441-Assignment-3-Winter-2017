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
		
		try {
				Thread.sleep(100); //Needed to ensure that a received ack is set before the time runs out
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			client.processTime(seqNum);	

		
		
	}

}
