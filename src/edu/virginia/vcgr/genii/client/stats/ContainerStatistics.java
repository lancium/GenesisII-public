package edu.virginia.vcgr.genii.client.stats;

public class ContainerStatistics
{
	static private ContainerStatistics _instance = new ContainerStatistics();
	
	static public ContainerStatistics instance()
	{
		return _instance;
	}

	private long _startTime;
	private DatabaseStatistics _dbStats = new DatabaseStatistics();
	private MethodStatistics _methodStats = new MethodStatistics();
	private DatabaseHistogramStatistics _dbHistoStats = new DatabaseHistogramStatistics();
	private MethodHistogramStatistics _methodHistoStats = new MethodHistogramStatistics();
	
	private ContainerStatistics()
	{
		_startTime = System.currentTimeMillis();
	}
	
	public long getStartTime()
	{
		return _startTime;
	}
	
	public DatabaseStatistics getDatabaseStatistics()
	{
		return _dbStats;
	}
	
	public DatabaseHistogramStatistics getDatabaseHistogramStatistics()
	{
		return _dbHistoStats;
	}
	
	public MethodStatistics getMethodStatistics()
	{
		return _methodStats;
	}
	
	public MethodHistogramStatistics getMethodHistogramStatistics()
	{
		return _methodHistoStats;
	}
}