package edu.virginia.vcgr.genii.container.cservices.percall;

import java.util.Calendar;
import java.util.Comparator;

class PersistentOutcallEntry
{
	private long _entryID;
	private int _numAttempts;
	private Calendar _nextAttempt;
	private Calendar _createTime;

	private AttemptScheduler _scheduler;

	PersistentOutcallEntry(long id, int numAttempts, Calendar nextAttempt, Calendar createTime, AttemptScheduler scheduler)
	{
		_entryID = id;
		_numAttempts = numAttempts;
		_nextAttempt = nextAttempt;
		_createTime = createTime;

		_scheduler = scheduler;
	}

	final long entryID()
	{
		return _entryID;
	}

	final int numAttempts()
	{
		return _numAttempts;
	}

	final void numAttempts(int numAttempts)
	{
		_numAttempts = numAttempts;
	}

	final Calendar nextAttempt()
	{
		return _nextAttempt;
	}

	final void nextAttempt(Calendar nextAttempt)
	{
		_nextAttempt = nextAttempt;
	}

	final Calendar createTime()
	{
		return _createTime;
	}

	final AttemptScheduler scheduler()
	{
		return _scheduler;
	}

	static private class NextAttemptComparator implements Comparator<PersistentOutcallEntry>
	{
		@Override
		public int compare(PersistentOutcallEntry o1, PersistentOutcallEntry o2)
		{
			int ret = o1._nextAttempt.compareTo(o2._nextAttempt);
			if (ret == 0) {
				long val = o1.entryID() - o2.entryID();
				if (val == 0L)
					return 0;
				else if (val < 0L)
					return -1;
				else
					return 1;
			}

			return ret;
		}
	}

	static final Comparator<PersistentOutcallEntry> NEXT_ATTEMPT_COMPARATOR = new NextAttemptComparator();
}