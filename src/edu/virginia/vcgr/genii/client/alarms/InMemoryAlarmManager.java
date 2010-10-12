package edu.virginia.vcgr.genii.client.alarms;

import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;
import java.util.PriorityQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class InMemoryAlarmManager
{
	static private Log _logger = LogFactory.getLog(InMemoryAlarmManager.class);
	
	static public InMemoryAlarmManager MANAGER =
		new InMemoryAlarmManager();
	
	private PriorityQueue<AlarmInformation> _queue =
		new PriorityQueue<AlarmInformation>();
	
	private InMemoryAlarmManager()
	{
		Thread th = new Thread(new InMemoryAlarmManagerWorker(),
			"In Memory Alarm Manager Worker Thread");
		th.setDaemon(true);
		th.start();
	}
	
	final private AlarmToken addAlarm(AlarmHandler handler, Object userData,
		Calendar nextOccurance, Long repeatInterval)
	{
		AlarmInformation info = new AlarmInformation(
			handler, userData, nextOccurance, repeatInterval);
		synchronized(_queue)
		{
			_queue.add(info);
			_queue.notify();
		}
		
		return info;
	}
	
	final public AlarmToken addAlarm(AlarmHandler handler, Object userData,
		Calendar occurance)
	{
		return addAlarm(handler, userData, occurance, null);
	}
	
	final public AlarmToken addAlarm(AlarmHandler handler,
		Calendar occurance)
	{
		return addAlarm(handler, null, occurance);
	}
	
	final public AlarmToken addAlarm(AlarmHandler handler, Object userData,
		long repeatInterval)
	{
		if (repeatInterval <= 0)
			throw new IllegalArgumentException(
				"Repeat interval must be positive.");
		
		Calendar nextOccurance = Calendar.getInstance();
		nextOccurance.setTimeInMillis(nextOccurance.getTimeInMillis() + repeatInterval);
		return addAlarm(handler, userData, nextOccurance, repeatInterval);
	}
	
	final public AlarmToken addAlarm(AlarmHandler handler, long repeatInterval)
	{
		return addAlarm(handler, null, repeatInterval);
	}
	
	private class InMemoryAlarmManagerWorker implements Runnable
	{
		@Override
		public void run()
		{
			Collection<AlarmInformation> alarmsToRun = 
				new LinkedList<AlarmInformation>();
			AlarmInformation info;
			
			while (true)
			{
				alarmsToRun.clear();
				Calendar now = Calendar.getInstance();
				
				synchronized(_queue)
				{
					while (true)
					{
						info = _queue.peek();
						if (info != null)
						{
							Calendar when = info.nextOccurance();
							if (when == null)
								_queue.remove();
							else if (when.compareTo(now) <= 0)
								alarmsToRun.add(_queue.remove());
							else
								break;
						} else
							break;
					}
				}
				
				for (AlarmInformation alarm : alarmsToRun)
					alarm.handleAlarm();
				
				synchronized(_queue)
				{
					for (AlarmInformation alarm : alarmsToRun)
					{
						if (alarm.nextOccurance() != null)
							_queue.add(alarm);
					}
					
					try
					{
						info = _queue.peek();
						if (info == null)
							_queue.wait();
						else
						{
							Calendar when = info.nextOccurance();
							long sleepTime;
							
							if (when == null)
								sleepTime = 1L;
							else
								sleepTime = when.getTimeInMillis() - 
									Calendar.getInstance().getTimeInMillis();
							
							if (sleepTime <= 0L)
								sleepTime = 1L;
							
							_queue.wait(sleepTime);
						}
					}
					catch (InterruptedException ie)
					{
						_logger.warn("In Memory Alarm Thread interrupted.",
							ie);
					}
				}
			}
		}
	}
}