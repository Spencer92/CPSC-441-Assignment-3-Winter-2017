package client.client;

import java.util.TimerTask;

public class SegmentProcessor extends Thread
{
	public SegmentProcessor()
	{
		
	}
	
	
	public synchronized void processSend(int num, Segment segment)
	{
		for(int i = 0; i < 10; i++)
		{
			System.out.println(num + ": Processing");
		}
	}
	
	public synchronized void processAck(Segment ack)
	{
		
	}
	
	public synchronized void processTime(int seqNum)
	{
		
	}
	
	private class TimeOutHandler extends TimerTask
	{

		@Override
		public void run() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
}
