package edu.virginia.vcgr.genii.client.utils.barrier;

import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BarrieredWorkQueue
{
	static private Log _logger = LogFactory.getLog(BarrieredWorkQueue.class);
	
	private LinkedList<Runnable> _workQueue =
		new LinkedList<Runnable>();
	private boolean _released = false;
	
	final private void run(Runnable runnable)
	{
		try
		{
			runnable.run();
		}
		catch (Throwable cause)
		{
			_logger.error("Error running barriered task.", cause);
		}
	}
	
	final public void enqueue(Runnable runnable)
	{
		boolean released;
		
		synchronized(_workQueue)
		{
			released = _released;
			if (!released)
				_workQueue.addLast(runnable);
		}
		
		if (released)
			run(runnable);
	}
	
	final public void release()
	{
		synchronized(_workQueue)
		{
			_released = true;
		}
		
		for (Runnable runnable : _workQueue)
			run(runnable);
	}
}