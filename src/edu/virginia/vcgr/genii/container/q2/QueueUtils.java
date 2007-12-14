package edu.virginia.vcgr.genii.container.q2;

import java.util.Calendar;
import java.util.Date;

/**
 * Just a collection (size 1 now) of useful utilities used by the queue.
 * 
 * @author mmm2a
 */
public class QueueUtils
{
	/**
	 * We occassionally need to convert a date object into a calendar
	 * object (SOAP uses calendars, everyone else uses dates).
	 * 
	 * @param d THe date to convert
	 * @return The new calendar instance of the date.
	 */
	static public Calendar convert(Date d)
	{
		if (d == null)
			return null;
		
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		return c;
	}
}