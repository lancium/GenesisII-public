package edu.virginia.vcgr.genii.container.cservices.gridlogger;

import java.util.Comparator;

import org.apache.log4j.spi.LoggingEvent;

import edu.virginia.vcgr.genii.client.gridlog.GridLogTarget;

class GridLogEvent
{
	static private class GridLogEventComparator implements Comparator<GridLogEvent>
	{
		@Override
		public int compare(GridLogEvent o1, GridLogEvent o2)
		{
			if (o1._nextAttempt < o2._nextAttempt)
				return -1;
			else if (o1._nextAttempt > o2._nextAttempt)
				return 1;
			else
				return 0;
		}
	}
	
	static final public Comparator<GridLogEvent> COMPARATOR =
		new GridLogEventComparator();
	
	private long _id;
	private GridLogTarget _target;
	private LoggingEvent _content;
	private short _numAttempts;
	
	private long _nextAttempt;
	
	GridLogEvent(long id, GridLogTarget target,
		LoggingEvent content, short numAttempts)
	{
		_id = id;
		_target = target;
		_content = content;
		_numAttempts = numAttempts;
		
		_nextAttempt = System.currentTimeMillis();
	}
	
	final long id()
	{
		return _id;
	}
	
	final GridLogTarget target()
	{
		return _target;
	}
	
	final LoggingEvent content()
	{
		return _content;
	}
	
	final short numAttempts()
	{
		return _numAttempts;
	}
	
	final short numAttempts(int delta)
	{
		_numAttempts += delta;
		return _numAttempts;
	}
	
	final long nextAttempt()
	{
		return _nextAttempt;
	}
	
	final long nextAttempt(long baseBackoff)
	{
		_nextAttempt = System.currentTimeMillis() +
			baseBackoff << _numAttempts;
		return _nextAttempt;
	}
}