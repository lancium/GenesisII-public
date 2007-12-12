package edu.virginia.vcgr.genii.container.q2;

import java.util.Date;

public class SortableJobKey implements Comparable<SortableJobKey>
{
	private long _jobID;
	private int _priority;
	private Date _submitTime;
	
	public SortableJobKey(long jobID, int priority, Date submitTime)
	{
		_jobID = jobID;
		_priority = priority;
		_submitTime = submitTime;
	}
	
	public SortableJobKey(long jobID)
	{
		this(jobID, 0, new Date());
	}
	
	public boolean equals(SortableJobKey other)
	{
		return other._jobID == _jobID;
	}
	
	public boolean equals(Object other) 
	{
		if (other instanceof SortableJobKey)
			return equals((SortableJobKey)other);
		
		return false;
	}
	
	public int compareTo(SortableJobKey other)
	{
		if (_jobID == other._jobID)
			return 0;
		
		int prioDiff = _priority - other._priority;
		if (prioDiff != 0)
			return prioDiff;
		
		if (_submitTime.before(other._submitTime))
			return -1;
		else if (_submitTime.after(other._submitTime))
			return 1;
		
		if (_jobID < other._jobID)
			return -1;
		
		return 1;
	}
}