package edu.virginia.vcgr.genii.client.utils.threads;

import java.util.Date;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ThreadPool
{
	static private Log _logger = LogFactory.getLog(ThreadPool.class);
	
	private SimpleAlarmManager _alarm;
	private LinkedList<Runnable> _workers;
	private int _coreSize;
	private int _maxSize;
	private int _activeThreads;
	private int _idleThreads;
	private long _idleTimeout;
	
	public ThreadPool(int coreSize, int maxSize, long idleTimeout)
	{
		_coreSize = coreSize;
		_maxSize = maxSize;
		_activeThreads = 0;
		_idleThreads = 0;
		_idleTimeout = idleTimeout;
		_workers = new LinkedList<Runnable>();
		
		_alarm = new SimpleAlarmManager();
	}
	
	public void enqueue(Runnable worker)
	{
		synchronized(_workers)
		{
			_workers.addLast(worker);
			
			if (_idleThreads <= 0)
			{
				// We don't have any threads waiting
				
				if (_activeThreads > _maxSize)
				{
					// Too many threads already, we'll have to wait
					return;
				}
				
				Thread th = new Thread(new WorkerThread());
				th.setDaemon(false);
				th.setName("Thread Pool Worker");
				_activeThreads++;
				th.start();
			} else
			{
				// There's an idle thread waiting
				_workers.notify();
			}
		}
	}
	
	public void schedule(Runnable worker, long delay)
	{
		schedule(worker, new Date(System.currentTimeMillis() + delay));
	}
	
	public void schedule(Runnable worker, Date time)
	{
		_alarm.addAlarm(new ScheduledActivityPlacer(worker), time);
	}
	
	private class ScheduledActivityPlacer implements Runnable
	{
		private Runnable _worker;
		
		public ScheduledActivityPlacer(Runnable worker)
		{
			_worker = worker;
		}
		
		public void run()
		{
			enqueue(_worker);
		}
	}
	
	private class WorkerThread implements Runnable
	{
		public void run()
		{
			while (true)
			{
				Runnable worker;
				
				synchronized(_workers)
				{
					long startTime = System.currentTimeMillis();
					while (_workers.isEmpty())
					{
						if (_idleThreads + _activeThreads > _coreSize)
						{
							// Too many threads, we're done
							_activeThreads--;
							return;
						}
						
						long idleTime = (System.currentTimeMillis() - startTime);
						if (idleTime > _idleTimeout)
						{
							// We've waited too long, time to go
							_activeThreads--;
							return;
						}
						
						_activeThreads--;
						_idleThreads++;
						try
						{
							_workers.wait(_idleTimeout - idleTime);
						}
						catch (InterruptedException ie)
						{
							_logger.warn("Thread in thread pool interrupted.");
						}
						catch (Throwable t)
						{
							_logger.error("Unknown exception in thread pool thread.", t);
						}
						_idleThreads--;
						_activeThreads++;
					}
					
					worker = _workers.removeFirst();
				}
				
				try
				{
					worker.run();
				}
				catch (Throwable t)
				{
					_logger.error("Job in thread pool threw an exception.", t);
				}
			}
		}	
	}
}