package edu.virginia.vcgr.genii.container.alarms;

import java.io.Serializable;

public class AlarmIdentifier implements Serializable
{
	static final long serialVersionUID = 0L;

	private long _alarmKey;

	public AlarmIdentifier(long alarmKey)
	{
		_alarmKey = alarmKey;
	}

	public long getAlarmKey()
	{
		return _alarmKey;
	}

	public void cancel()
	{
		AlarmManager.getManager().cancelAlarm(_alarmKey);
	}

	public boolean equals(AlarmIdentifier other)
	{
		return _alarmKey == other._alarmKey;
	}

	@Override
	public boolean equals(Object other)
	{
		if (other instanceof AlarmIdentifier)
			return equals((AlarmIdentifier) other);

		return false;
	}

	@Override
	public int hashCode()
	{
		return (int) _alarmKey;
	}

	@Override
	public String toString()
	{
		return Long.toString(_alarmKey);
	}
}