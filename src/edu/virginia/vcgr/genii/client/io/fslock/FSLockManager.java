package edu.virginia.vcgr.genii.client.io.fslock;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class FSLockManager
{
	private class ReferenceCountLock implements FSLock
	{
		private String _path;
		private int _referenceCount = 0;
		private Semaphore _semaphore = new Semaphore(1);
		
		private ReferenceCountLock(String path)
		{
			_path = path;
		}
		
		private int increment()
		{
			return ++_referenceCount;
		}
		
		private int decrement()
		{
			return --_referenceCount;
		}
		
		private void acquire()
		{
			_semaphore.acquireUninterruptibly();
		}
		
		@Override
		public void release()
		{
			_semaphore.release();
			
			synchronized(_locks)
			{
				if (decrement() <= 0)
					_locks.remove(_path);
			}
		}
	}
	
	private Map<String, ReferenceCountLock> _locks =
		new HashMap<String, ReferenceCountLock>();
	
	public FSLock acquire(File file) throws IOException
	{
		String path = file.getCanonicalPath();
		
		ReferenceCountLock count;
		
		synchronized(_locks)
		{
			count = _locks.get(path);
			if (count == null)
				_locks.put(path, count = new ReferenceCountLock(path));
			
			count.increment();
		}
		
		count.acquire();
		return count;
	}
}