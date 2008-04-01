package edu.virginia.vcgr.genii.container.q2;

import java.io.Closeable;
import java.io.IOException;
import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * A simple thread pool class.  This class doesn't do anything fancy with
 * creating/timing out threads.  It simply creates the number asked for and
 * keeps them alive.
 * 
 * @author mmm2a
 */
public class ThreadPool implements Closeable
{
	static private Log _logger = LogFactory.getLog(ThreadPool.class);
	
	volatile private boolean _closing = false;
	
	/**
	 * The list of workers that are waiting for an opportunity to run.
	 */
	private LinkedList<Runnable> _queue = new LinkedList<Runnable>();
	
	/**
	 * The array of threads in this thread pool.
	 */
	private Thread []_threads;
	
	/**
	 * Create a new thread pool with the indicated number of threads.
	 * 
	 * @param maxSize The number of threads to allocate.
	 */
	public ThreadPool(int maxSize)
	{
		if (maxSize <= 0)
			throw new IllegalArgumentException("maxSize MUST be greater than 0.");
		
		/*
		 * Create all of the threads and start them running.
		 */
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
		synchronized(_queue)
		{
			_queue.notifyAll();
		}
	}
	
	/**
	 * Enqueue a new worker into the thread pool.
	 * 
	 * @param job The Worker looking for a thread to run on.
	 */
	public void enqueue(Runnable job)
	{
		synchronized(_queue)
		{
			/* Add this worker to the end of the queue */
			_queue.addLast(job);
			
			/* And notify the threads that a new task is available. */
			_queue.notifyAll();
		}
	}
	
	/**
	 * This is the internal thread runner class.  It's job is to wait for a
	 * worker task to get enqueue, then run it and go back to sleep.
	 * 
	 * @author mmm2a
	 */
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
						/* While the queue is empty, we simply wait. */
						while (_queue.isEmpty() && !_closing)
							_queue.wait();
						
						if (_closing)
							break;
						
						/* Pull the first task off of the queue */
						job = _queue.removeFirst();
					}
					
					try
					{
						/* Run the task.  Make sure we catch all exceptions so
						 * that the thread doesn't exit prematurely.
						 */
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