package edu.virginia.vcgr.genii.container.q2;

public class BESData
{
	private long _besID;
	private String _besName;
	private int _totalSlots;
	
	public BESData(long besID, String besName, int totalSlots)
	{
		_besID = besID;
		_besName = besName;
		_totalSlots = totalSlots;
	}
	
	public long getID()
	{
		return _besID;
	}
	
	public String getName()
	{
		return _besName;
	}
	
	public int getTotalSlots()
	{
		return _totalSlots;
	}
	
	public void setTotalSlots(int totalSlots)
	{
		_totalSlots = totalSlots;
	}
}