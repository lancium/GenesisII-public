package edu.virginia.vcgr.genii.container.alarms;

import java.util.Date;

public class AlarmDescriptor
{
	private long _alarmID;
	private Date _nextOccurance;

	public AlarmDescriptor(long alarmID, Date nextOccurance)
	{
		_alarmID = alarmID;
		_nextOccurance = nextOccurance;
	}

	public long getAlarmID()
	{
		return _alarmID;
	}

	public Date getNextOccurance()
	{
		return _nextOccurance;
	}
}