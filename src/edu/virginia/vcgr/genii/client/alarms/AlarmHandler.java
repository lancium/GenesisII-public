package edu.virginia.vcgr.genii.client.alarms;

public interface AlarmHandler
{
	public void alarmWentOff(AlarmToken token, Object userData);
}