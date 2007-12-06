package edu.virginia.vcgr.genii.container.q2;

import java.io.Closeable;
import java.io.IOException;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ThreadPool implements Closeable
{
	static private Log _logger = LogFactory.getLog(ThreadPool.class);
	
	volatile private boolean _closing = false;
	private LinkedList<Runnable> _queue = new LinkedList<Runnable>();
	private Thread []_threads;
	
	public ThreadPool(int maxSize)
	{
		if (maxSize <= 0)
			throw new IllegalArgumentException("maxSize MUST be greater than 0.");
		
		_threads = new Thread[maxSize];
		for (int lcv = 0; lcv < maxSize; lcv++)
		{
			_threads[lcv] = new Thread(new ThreadWorker());
			_threads[lcv].setDaemon(true);
			_threads[lcv].setName("ThreadPool Worker Thread");
			
			_threads[lcv].start();
		}
	}
	
	protected void finalize() throws Throwable
	{
		close();
	}
	
	synchronized public void close() throws IOException
	{
		if (_closing)
			return;
		
		_closing = true;
		for (Thread th : _threads)
		{
			th.interrupt();
		}
	}
	
	public void enqueue(Runnable job)
	{
		synchronized(_queue)
		{
			_queue.addLast(job);
			_queue.notifyAll();
		}
	}
	
	private class ThreadWorker implements Runnable
	{
		public void run()
		{
			Runnable job;
			
			while(!_closing)
			{
				try
				{
					synchronized(_queue)
					{
						while (_queue.isEmpty())
							_queue.wait();
						
						job = _queue.removeFirst();
					}
					
					try
					{
						job.run();
					}
					catch (Throwable cause)
					{
						_logger.warn("Thread Pool Job threw exception.", cause);
					}
				}
				catch (InterruptedException ie)
				{
					Thread.interrupted();
				}
			}
		}
	}
}