package edu.virginia.vcgr.genii.client.utils.refcount;

import java.io.Closeable;

public class LockWrapper<KeyType> implements Closeable
{
	private LockManager<KeyType> _manager;
	private Object _lockObject;
	private KeyType _key;

	LockWrapper(LockManager<KeyType> manager, KeyType key, Object lockObject)
	{
		_manager = manager;
		_lockObject = lockObject;
		_key = key;
	}

	protected void finalize() throws Throwable
	{
		try {
			close();
		} finally {
			super.finalize();
		}
	}

	public Object getLockObject()
	{
		return _lockObject;
	}

	public void close()
	{
		synchronized (this) {
			if (_lockObject != null) {
				_manager.releaseLockWrapper(_key);
				_manager = null;
				_key = null;
				_lockObject = null;
			}
		}
	}
}