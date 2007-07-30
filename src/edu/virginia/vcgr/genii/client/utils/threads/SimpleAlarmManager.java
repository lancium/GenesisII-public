package edu.virginia.vcgr.genii.client.utils.threads;

import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.PriorityQueue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SimpleAlarmManager implements Runnable
{
	static private Log _logger = LogFactory.getLog(SimpleAlarmManager.class);
	
	static private class AlarmEntry
	{
		private Date _occurrance;
		private Runnable _worker;
		
		public AlarmEntry(Date occurrance, Runnable worker)
		{
			_occurrance = occurrance;
			_worker = worker;
		}
		
		public Date getOccurance()
		{
			return _occurrance;
		}
		
		public Runnable getWorker()
		{
			return _worker;
		}
	}
	
	static private class OccuranceComparator 
		implements Comparator<AlarmEntry>
	{
		public int compare(AlarmEntry o1, AlarmEntry o2)
		{
			Date o1o = o1.getOccurance();
			Date o2o = o2.getOccurance();
			
			if (o1o.before(o2o))
				return -1;
			else if (o1o.after(o2o))
				return 1;
			else
				return 0;
		}	
	}
	
	private PriorityQueue<AlarmEntry> _alarms =
		new PriorityQueue<AlarmEntry>(32, new OccuranceComparator());

	public SimpleAlarmManager()
	{
		Thread th = new Thread(this);
		th.setDaemon(false);
		th.setName("Simple Alarm Manager Thread");
		th.start();
	}
	
	public void addAlarm(Runnable worker, Date occurance)
	{
		AlarmEntry entry = new AlarmEntry(occurance, worker);
		
		synchronized(_alarms)
		{
			_alarms.add(entry);
			_alarms.notify();
		}
	}
	
	public void run()
	{
		AlarmEntry entry;
		Date entryOccurance;
		long timeLeft;
		
		LinkedList<AlarmEntry> elapsedEntries = new LinkedList<AlarmEntry>();
		
		while (true)
		{
			synchronized(_alarms)
			{
				try
				{
					if (_alarms.isEmpty())
					{
						_alarms.wait();
						continue;
					}
	
					entry = _alarms.peek();
					entryOccurance = entry.getOccurance();
					timeLeft = 
						entryOccurance.getTime() - System.currentTimeMillis();
					if (timeLeft > 0)
					{
						_alarms.wait(timeLeft);
						continue;
					}
				}
				catch (InterruptedException ie)
				{
					_logger.warn("SimpleAlarmManager Thread Interrupted.");
					continue;
				}
				catch (Throwable t)
				{
					_logger.error("Unknown exception in alarm thread.", t);
					continue;
				}
				
				// If we get this far, there are things to be done.
				elapsedEntries.clear();
				while (true)
				{
					entry = _alarms.peek();
					entryOccurance = entry.getOccurance();
					timeLeft = 
						entryOccurance.getTime() - System.currentTimeMillis();
					if (timeLeft > 0)
						break;
					elapsedEntries.add(_alarms.remove());
				}
			}
			
			for (AlarmEntry entry2 : elapsedEntries)
			{
				try
				{
					entry2.getWorker().run();
				}
				catch (Throwable t)
				{
					_logger.error("Error occurred during alarm handling.");
				}
			}
		}
	}
}