package edu.virginia.vcgr.genii.container.stats;

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
	
	public MethodStatistics getMethodStatistics()
	{
		return _methodStats;
	}
}