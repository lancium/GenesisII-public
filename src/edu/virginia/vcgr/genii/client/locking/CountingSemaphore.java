package edu.virginia.vcgr.genii.client.locking;

public class CountingSemaphore implements GLock
{
	private int _count;
	private Object _mutex = new Object();
	
	public CountingSemaphore(int maxCount)
	{
		_count = maxCount;
	}
	
	@Override
	public void lock()
	{
		synchronized(_mutex)
		{
			while (_count <= 0)
			{
				try
				{
					_mutex.wait();
				}
				catch (InterruptedException ie)
				{
					Thread.interrupted();
				}
			}
			
			_count--;
		}
	}

	@Override
	public void unlock()
	{
		synchronized(_mutex)
		{
			_count++;
			_mutex.notify();
		}
	}
}