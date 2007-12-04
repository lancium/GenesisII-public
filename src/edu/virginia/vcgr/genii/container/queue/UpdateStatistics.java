package edu.virginia.vcgr.genii.container.queue;

public class UpdateStatistics
{
	private int _totalResources;
	private int _totalAvailable;
	
	public UpdateStatistics(int total, int totalAvail)
	{
		_totalResources = total;
		_totalAvailable = totalAvail;
	}
	
	public int getTotalResources()
	{
		return _totalResources;
	}
	
	public int getTotalAvailable()
	{
		return _totalAvailable;
	}
}