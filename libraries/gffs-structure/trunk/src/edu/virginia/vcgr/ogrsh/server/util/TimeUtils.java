package edu.virginia.vcgr.ogrsh.server.util;

import java.util.Date;

public class TimeUtils
{
	static public long getSeconds(Date date)
	{
		return date.getTime() / 1000;
	}
}