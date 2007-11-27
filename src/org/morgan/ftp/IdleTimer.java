package org.morgan.ftp;

public class IdleTimer
{
	private long _lastActivity;
	
	public IdleTimer()
	{
		_lastActivity = System.currentTimeMillis();
	}
	
	public void noteActivity()
	{
		_lastActivity = System.currentTimeMillis();
	}
	
	public long idleTime()
	{
		return System.currentTimeMillis() - _lastActivity;
	}
}