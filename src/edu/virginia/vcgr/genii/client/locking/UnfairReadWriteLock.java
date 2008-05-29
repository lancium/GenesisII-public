package edu.virginia.vcgr.genii.client.locking;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UnfairReadWriteLock implements GReadWriteLock
{
	static private Log _logger = LogFactory.getLog(UnfairReadWriteLock.class);
	
	private int _readers = 0;
	private int _writers = 0;
	private Object _mutex = new Object();
	
	@Override
	public GLock readLock()
	{
		return new ReadLockImpl();
	}

	@Override
	public GLock writeLock()
	{
		return new WriteLockImpl();
	}
	
	private class ReadLockImpl implements GLock
	{
		@Override
		public void lock()
		{
			synchronized(_mutex)
			{
				while (_writers > 0)
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
			
				_readers++;
				_logger.debug(String.format(
					"Just acquired a read lock[%d readers, %d writers]\n",
					_readers, _writers));
			}
		}

		@Override
		public void unlock()
		{
			synchronized(_mutex)
			{
				_readers--;
				if (_readers == 0)
					_mutex.notify();
				_logger.debug(String.format(
					"Just released a read lock[%d readers, %d writers]\n",
					_readers, _writers));
			}
		}
	}
	
	private class WriteLockImpl implements GLock
	{
		@Override
		public void lock()
		{
			synchronized(_mutex)
			{
				while (_readers > 0 || _writers > 0)
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
				
				_writers++;
				_logger.debug(String.format(
					"Just acquired a write lock[%d readers, %d writers]\n",
					_readers, _writers));
			}
		}

		@Override
		public void unlock()
		{
			synchronized(_mutex)
			{
				_writers--;
				if (_writers == 0)
					_mutex.notifyAll();
				_logger.debug(String.format(
					"Just released a write lock[%d readers, %d writers]\n",
					_readers, _writers));
			}
		}
	}
}