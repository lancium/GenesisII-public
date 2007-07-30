package edu.virginia.vcgr.genii.client.utils;

import java.io.Closeable;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.configuration.NamedInstances;

public class BoundedThreadPool implements Closeable
{
	static private Log _logger = LogFactory.getLog(BoundedThreadPool.class);
	
	volatile private boolean _closed = false;
	private BoundedBlockingQueue<Runnable> _queue;
	private Thread []_threads;
	
	public BoundedThreadPool(BoundedBlockingQueue<Runnable> queue)
	{
		_queue = queue;
		_threads = new Thread[_queue.getCapacity()];
		ThreadFactory factory = (ThreadFactory)NamedInstances.getRoleBasedInstances(
			).lookup("thread-factory");
		for (int lcv = 0; lcv < _threads.length; lcv++)
		{
			Thread th = _threads[lcv] = factory.newThread(
				new BoundedThreadPoolWorker());
			th.setName("BoundedThreadPoolWorker");
			th.setDaemon(false);
			th.start();
		}
	}
	
	protected void finalize() throws Throwable
	{
		try
		{
			close();
		}
		finally
		{
			super.finalize();
		}
	}
	public void close()
	{
		_closed = true;
		
		synchronized(_threads)
		{
			for (Thread th : _threads)
			{
				th.interrupt();
			}
		}
	}
	
	public void enqueue(Runnable work) throws InterruptedException
	{
		_queue.add(work);
	}
	
	private class BoundedThreadPoolWorker implements Runnable
	{
		public void run()
		{
			// Mark Morgan -- this was an attempt to recyle threads that failed to
			// produce useful results.
			// int count = 0;
			
			while (!_closed)
			{
				// Mark Morgan -- this was an attempt to recyle threads that failed to
				// produce useful results.
				// count++;
				
				try
				{
					Runnable work = _queue.remove();
					work.run();
				}
				catch (InterruptedException ie)
				{
				}
				catch (Throwable t)
				{
					_logger.warn(
						"Worker threw exception in BoundedThreadPoolWorker.", t);
				}
				
				// Mark Morgan -- this was an attempt to recyle threads that failed to
				// produce useful results.
				// if (count > 10)
				// {
				//	break;
				// }
			}
			
			/* Mark Morgan -- this was an attempt to recyle threads that failed to
			 * produce useful results.
			if (!_closed)
			{
				// restart thread
				ThreadFactory factory = (ThreadFactory)NamedInstances.getRoleBasedInstances(
					).lookup("thread-factory");
				Thread th = factory.newThread(this);
				
				synchronized(_threads)
				{
					_threads[_myThreadNumber] = th;
				}
				
				th.setName("BoundedThreadPoolWorker");
				th.setDaemon(false);
				th.start();
			}
			*/
		}
	}
}