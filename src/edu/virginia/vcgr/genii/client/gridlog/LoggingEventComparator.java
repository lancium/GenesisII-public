package edu.virginia.vcgr.genii.client.gridlog;

import java.util.Comparator;

import edu.virginia.vcgr.genii.container.gridlog.LogEventInformation;

public class LoggingEventComparator implements Comparator<LogEventInformation>
{
	static final public Comparator<LogEventInformation> COMPARATOR =
		new LoggingEventComparator();
	
	private LoggingEventComparator()
	{
	}
	
	@Override
	public int compare(LogEventInformation o1, LogEventInformation o2)
	{
		if (o1.event().timeStamp < o2.event().timeStamp)
			return -1;
		else if (o1.event().timeStamp > o2.event().timeStamp)
			return 1;
		else
			return 0;
	}
}