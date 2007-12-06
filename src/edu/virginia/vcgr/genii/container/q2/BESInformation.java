package edu.virginia.vcgr.genii.container.q2;

import java.util.Date;
import java.util.LinkedList;

public class BESInformation
{
	private long _resourceid;
	private String _resourceName;
	private int _totalSlots;
	private LinkedList<Long> _runningJobs = new LinkedList<Long>();
	private boolean _isResponsive;
	private Date _lastUpdated;
	private int _updatesSinceLastSuccess;
	
	public BESInformation(long resourceid, String resourceName, int totalSlots)
	{
		_resourceid = resourceid;
		_resourceName = resourceName;
		_totalSlots = totalSlots;
		_isResponsive = false;
		_lastUpdated = new Date(0);
		_updatesSinceLastSuccess = 0;
	}
	
	public long getResourceID()
	{
		return _resourceid;
	}
	
	public String getResourceName()
	{
		return _resourceName;
	}
	
	public int getTotalSlots()
	{
		return _totalSlots;
	}
	
	public boolean isResponsive()
	{
		return _isResponsive;
	}
	
	public Date getLastUpdated()
	{
		return _lastUpdated;
	}
	
	public void setTotalSlots(int totalSlots)
	{
		_totalSlots = totalSlots;
	}
	
	public void update(boolean isResp)
	{
		_isResponsive = isResp;
		_lastUpdated = new Date();
		if (isResp)
			_updatesSinceLastSuccess = 0;
		else
			_updatesSinceLastSuccess++;
	}
	
	public int updatesSinceLastSuccess()
	{
		return _updatesSinceLastSuccess;
	}
	
	public int slotsAvailable()
	{
		return _totalSlots - _runningJobs.size();
	}
	
	public void manageJob(long jobid)
	{
		_runningJobs.add(new Long(jobid));
	}
	
	public void forgetJob(long jobid)
	{
		_runningJobs.remove(new Long(jobid));
	}
	
	public Date getNextUpdate(long baseInterval, int maxMisses)
	{
		if (_updatesSinceLastSuccess < maxMisses)
			maxMisses = _updatesSinceLastSuccess;
		
		long interval = baseInterval << maxMisses;
		return new Date(_lastUpdated.getTime() + interval);
	}
}