package client.client;

import java.util.Timer;
import java.util.TimerTask;

import client.Queue.TxQueue;
import client.Queue.TxQueueNode;

public class TimerThreadWait extends TimerTask
{
	Segment packet = null;
	long delay = -1;
	int num = 0;
	
	public TimerThreadWait(Segment packet, long delay)
	{
		this.packet = packet;
		this.delay = delay;
	}
	
	@Override
	public void run() 
	{
		Timer aTimer = new Timer();
		
		
		
		aTimer.schedule(new TimerThreadWait(this.packet, this.delay), this.delay);
		//recieve ack
		//check if correct ack
		//if it is, remove head
		//if not, remove head and add to end of queue
		//maybe
		
		
	}

}
