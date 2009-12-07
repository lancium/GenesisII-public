package edu.virginia.vcgr.genii.container.cservices.percall;

import java.util.Calendar;
import java.util.Comparator;

class PersistentOutcallEntry
{
	private long _entryID;
	private int _numAttempts;
	private Calendar _nextAttempt;
	private Calendar _createTime;
	
	private OutcallActor _outcallActor;
	private AttemptScheduler _scheduler;
	
	PersistentOutcallEntry(long id,
		int numAttempts, Calendar nextAttempt, Calendar createTime,
		OutcallActor outcallActor, AttemptScheduler scheduler)
	{
		_entryID = id;
		_numAttempts = numAttempts;
		_nextAttempt = nextAttempt;
		_createTime = createTime;
		
		_outcallActor = outcallActor;
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
	
	final OutcallActor outcallActor()
	{
		return _outcallActor;
	}
	
	final AttemptScheduler scheduler()
	{
		return _scheduler;
	}
	
	static private class NextAttemptComparator
		implements Comparator<PersistentOutcallEntry>
	{
		@Override
		public int compare(PersistentOutcallEntry o1, PersistentOutcallEntry o2)
		{
			return o1._nextAttempt.compareTo(o2._nextAttempt);
		}
	}
	
	static final Comparator<PersistentOutcallEntry> NEXT_ATTEMPT_COMPARATOR =
		new NextAttemptComparator();
}