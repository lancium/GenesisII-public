package edu.virginia.vcgr.genii.client.stats;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

public class DatabaseStatistics
{
	static private final long WINDOW_SIZE = TimeInterval.FIVE_MINUTES.durationMS();
	
	private long _databaseStartTime;
	
	private LinkedList<DBConnectionDataPoint> _dataPoints =
		new LinkedList<DBConnectionDataPoint>();
	
	private void trim()
	{
		long now = System.currentTimeMillis();
		Iterator<DBConnectionDataPoint> iter = _dataPoints.iterator();
		while (iter.hasNext())
		{
			if (iter.next().withinWindow(now, WINDOW_SIZE))
				break;
			
			iter.remove();
		}
	}
	
	public DatabaseStatistics()
	{
		resetDatabase();
	}
	
	public DBConnectionDataPoint openConnection()
	{
		DBConnectionDataPoint dp = new DBConnectionDataPoint();
		
		synchronized(_dataPoints)
		{
			trim();
			
			_dataPoints.addLast(dp);
		}
		
		return dp;
	}
	
	public void resetDatabase()
	{
		_databaseStartTime = System.currentTimeMillis();
	}
	
	public long databaseStartTime()
	{
		return _databaseStartTime;
	}
	
	public Map<TimeInterval, DatabaseStatisticsReport> report()
	{
		Map<TimeInterval, DatabaseStatisticsReport> report = 
			new EnumMap<TimeInterval, DatabaseStatisticsReport>(
				TimeInterval.class);
		
		for (TimeInterval ti : TimeInterval.values())
			report.put(ti, new DatabaseStatisticsReport());
		
		long now = System.currentTimeMillis();
		
		synchronized(_dataPoints)
		{
			trim();
			
			for (DBConnectionDataPoint dataPoint : _dataPoints)
			{
				for (TimeInterval ti : TimeInterval.values())
				{
					if (dataPoint.withinWindow(now, ti.durationMS()))
						report.get(ti).add(dataPoint);
				}
			}
		}
		
		return report;
	}
}