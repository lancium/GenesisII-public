package edu.virginia.vcgr.genii.client.locking;

public class FairCountingReadWriteLock implements GReadWriteLock
{
	private int _maxReaders;
	private GLock _semaphore;
	private Object _mutex = new Object();

	public FairCountingReadWriteLock(int maxReaders)
	{
		_maxReaders = maxReaders;
		_semaphore = new CountingSemaphore(maxReaders);
	}

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
			_semaphore.lock();
		}

		@Override
		public void unlock()
		{
			_semaphore.unlock();
		}
	}

	private class WriteLockImpl implements GLock
	{
		@Override
		public void lock()
		{
			synchronized (_mutex) {
				for (int lcv = 0; lcv < _maxReaders; lcv++) {
					_semaphore.lock();
				}
			}
		}

		@Override
		public void unlock()
		{
			for (int lcv = 0; lcv < _maxReaders; lcv++) {
				_semaphore.unlock();
			}
		}
	}
}