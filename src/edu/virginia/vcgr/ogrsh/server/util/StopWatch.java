package edu.virginia.vcgr.ogrsh.server.util;

import java.util.Date;

public final class StopWatch
{
	private Date _startTime;
	
	public StopWatch()
	{
		_startTime = null;
	}
	
	final public void start()
	{
		_startTime = new Date();
	}
	
	final public double lap()
	{
		Date d = new Date();
		long elapsed = d.getTime() - _startTime.getTime();
		double ret = elapsed / 1000.0;
		_startTime = new Date();
		return ret;
	}
}