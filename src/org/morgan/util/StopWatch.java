package org.morgan.util;

public class StopWatch
{
	private long _start;
	
	public StopWatch()
	{
		_start = System.nanoTime();
	}
	
	public void start()
	{
		_start = System.nanoTime();
	}
	
	public double lap()
	{
		long elapsed = System.nanoTime() - _start;
		double ret = elapsed / 1000000000.0;
		_start = System.nanoTime();
		return ret;
	}
}