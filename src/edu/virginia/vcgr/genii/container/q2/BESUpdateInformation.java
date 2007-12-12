package edu.virginia.vcgr.genii.container.q2;

import java.util.Date;

public class BESUpdateInformation
{
	private long _updateCycle;
	private long _besID;
	private Date _lastUpdated;
	private Date _lastResponsive;
	
	public BESUpdateInformation(long besID, long updateCycle)
	{
		_besID = besID;
		_lastResponsive = null;
		_lastUpdated = null;
		_updateCycle = updateCycle;
	}
	
	public long getBESID()
	{
		return _besID;
	}
	
	synchronized public void update(boolean isResponsive)
	{
		_lastUpdated = new Date();
		_lastResponsive = isResponsive ? _lastUpdated : _lastResponsive;
	}
	
	synchronized public boolean isResponsive()
	{
		if (_lastResponsive == null || _lastUpdated == null)
			return false;
		
		return !_lastResponsive.before(_lastUpdated);
	}
	
	public boolean timeForUpdate(Date now)
	{
		if (_lastUpdated == null)
			return true;
		
		return (now.getTime() - _lastUpdated.getTime() >= _updateCycle);
	}
}