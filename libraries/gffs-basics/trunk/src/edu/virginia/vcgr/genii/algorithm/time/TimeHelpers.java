package edu.virginia.vcgr.genii.algorithm.time;

// is there no better set of classes for time in base java stuff?
// this class exists because we didn't find a convenient way to do these operations.

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

}
