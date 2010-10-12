package edu.virginia.vcgr.genii.client.alarms;

import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

class AlarmInformation implements AlarmToken, Comparable<AlarmInformation>
{
	static private Log _logger = LogFactory.getLog(AlarmInformation.class);
	
	private AlarmHandler _handler = null;
	private Object _userData = null;
	private Calendar _nextOccurance;
	private Long _repeatInterval = null;
	
	AlarmInformation(AlarmHandler handler, Object userData,
		Calendar nextOccurance, Long repeatInterval)
	{
		if (handler == null)
			throw new IllegalArgumentException(
				"Alarm handler cannot be null.");
		
		if (nextOccurance == null)
			throw new IllegalArgumentException(
				"Next occurance cannot be null.");
		
		_userData = userData;
		_handler = handler;
		_nextOccurance = nextOccurance;
		_repeatInterval = repeatInterval;
	}
	
	final Calendar handleAlarm()
	{
		if (_nextOccurance == null)
			return null;
		
		try
		{
			_handler.alarmWentOff(this, _userData);
		}
		catch (Throwable cause)
		{
			_logger.warn("Alarm handler threw exception.", cause);
		}
		
		synchronized (this)
		{
			if ((_nextOccurance != null) && (_repeatInterval != null))
			{
				_nextOccurance = Calendar.getInstance();
				_nextOccurance.setTimeInMillis(
					_nextOccurance.getTimeInMillis() + _repeatInterval);
			} else
				_nextOccurance = null;
		}
		
		return _nextOccurance;
	}
	
	@Override
	synchronized final public void cancel()
	{
		_nextOccurance = null;
	}

	@Override
	synchronized final public Calendar nextOccurance()
	{
		return _nextOccurance;
	}

	@Override
	final public boolean isRepeating()
	{
		return _repeatInterval != null;
	}

	@Override
	final public int compareTo(AlarmInformation o)
	{
		Calendar a = nextOccurance();
		Calendar b = o.nextOccurance();
		
		if (a == null)
		{
			if (b == null)
				return 0;
			else
				return -1;
		} else
		{
			if (b == null)
				return 1;
			else
				return a.compareTo(b);
		}
	}
}