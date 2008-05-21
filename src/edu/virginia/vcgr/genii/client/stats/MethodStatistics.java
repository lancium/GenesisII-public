package edu.virginia.vcgr.genii.client.stats;

import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class MethodStatistics
{
	static private final long WINDOW_SIZE = TimeInterval.FIVE_MINUTES.durationMS();
	
	private LinkedList<MethodDataPoint> _dataPoints =
		new LinkedList<MethodDataPoint>();
	
	private void trim()
	{
		long now = System.currentTimeMillis();
		Iterator<MethodDataPoint> iter = _dataPoints.iterator();
		while (iter.hasNext())
		{
			if (iter.next().withinWindow(now, WINDOW_SIZE))
				break;
			
			iter.remove();
		}
	}

	public MethodDataPoint startMethod(Class<?> serviceClass, Method serviceMethod)
	{
		MethodDataPoint dp = new MethodDataPoint(serviceClass, serviceMethod);
		
		synchronized(_dataPoints)
		{
			trim();
			
			_dataPoints.addLast(dp);
		}
		
		return dp;
	}
	
	public Map<TimeInterval, MethodStatisticsReport> report() 
	{
		Map<TimeInterval, MethodStatisticsReport> report =
			new EnumMap<TimeInterval, MethodStatisticsReport>(
				TimeInterval.class);
		
		for (TimeInterval ti : TimeInterval.values())
			report.put(ti, new MethodStatisticsReport());
		
		long currentTime = System.currentTimeMillis();
		
		synchronized(_dataPoints)
		{
			trim();
			
			for (MethodDataPoint dp : _dataPoints)
			{
				for (TimeInterval ti : TimeInterval.values())
				{
					if (dp.withinWindow(currentTime, ti.durationMS()))
						report.get(ti).add(dp);
				}
			}
		}
		
		return report;
	}
}