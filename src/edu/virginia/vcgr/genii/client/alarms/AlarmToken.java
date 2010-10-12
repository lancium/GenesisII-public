package edu.virginia.vcgr.genii.client.alarms;

import java.util.Calendar;

public interface AlarmToken
{
	public void cancel();
	public Calendar nextOccurance();
	public boolean isRepeating();
}