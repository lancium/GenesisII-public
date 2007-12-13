package edu.virginia.vcgr.genii.container.q2;

import java.util.Calendar;
import java.util.Date;

public class QueueUtils
{
	static public Calendar convert(Date d)
	{
		if (d == null)
			return null;
		
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		return c;
	}
}