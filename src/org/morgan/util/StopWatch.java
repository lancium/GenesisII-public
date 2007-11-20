package org.morgan.util;

import java.util.Date;

public class StopWatch
{
	private long _start;
	
	public StopWatch()
	{
		_start = System.currentTimeMillis();
	}
	
	public void start()
	{
		_start = System.currentTimeMillis();
	}
	
	public double lap()
	{
		long elapsed = System.currentTimeMillis() - _start;
		double ret = elapsed / 1000.0;
		_start = System.currentTimeMillis();
		return ret;
	}
}