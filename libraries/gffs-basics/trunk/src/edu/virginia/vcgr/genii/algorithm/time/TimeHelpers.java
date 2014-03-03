package edu.virginia.vcgr.genii.algorithm.time;

public class TimeHelpers
{
	// converts a number of milliseconds to a number of days.
	public static double millisToDays(double milliseconds)
	{
		return milliseconds / 1000.0 / 60.0 / 60.0 / 24.0;
	}

	// converts milliseconds into years.
	public static double millisToYears(double milliseconds)
	{
		return milliseconds / 1000.0 / 60.0 / 60.0 / 24.0 / 365.25;
	}

	public static long _appStartMillis = millisSinceBoot();

	/**
	 * returns the number of milliseconds since the computer booted.
	 */
	public static long millisSinceBoot()
	{
		return System.nanoTime() / 1000000;
	}

	/**
	 * returns the number of milliseconds since the program started. this is usually a much nicer
	 * number than the milliseconds since boot.
	 */
	public static long millisSinceAppStart()
	{
		return millisSinceBoot() - _appStartMillis;
	}
}
