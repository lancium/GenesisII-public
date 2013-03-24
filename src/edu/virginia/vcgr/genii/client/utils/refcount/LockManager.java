package edu.virginia.vcgr.genii.client.utils.refcount;

import java.util.HashMap;

public class LockManager<KeyType>
{
	static private class Counter
	{
		private int _count = 0;

		public void increment()
		{
			_count++;
		}

		public boolean decrement()
		{
			return (--_count) == 0;
		}
	}

	private HashMap<KeyType, Counter> _counters = new HashMap<KeyType, Counter>();

	public LockWrapper<KeyType> acquireLockWrapper(KeyType key)
	{
		Counter c;
		synchronized (_counters) {
			c = _counters.get(key);
			if (c == null) {
				c = new Counter();
				_counters.put(key, c);
			}

			c.increment();
		}

		return new LockWrapper<KeyType>(this, key, c);
	}

	void releaseLockWrapper(KeyType key)
	{
		Counter c;

		synchronized (_counters) {
			c = _counters.get(key);
			if (c == null)
				throw new IllegalStateException("Counter underflow in locking.");

			if (c.decrement()) {
				_counters.remove(key);
			}
		}
	}
}