package edu.virginia.vcgr.genii.container.q2;

import java.util.Date;

/**
 * We need to be able to sort jobs by their priority and submit time.  This
 * class groups together information about a job necessary to do that sort.
 * 
 * @author mmm2a
 */
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
	
	public SortableJobKey(JobData jobData)
	{
		this(jobData.getJobID(), 
			jobData.getPriority(), jobData.getSubmitTime());
	}
	
	/**
	 * Two SortableJobKey's are equal if their jobIDs are equals
	 * 
	 * @param other THe other job key against which to compare.
	 * 
	 * @return True if the two job keys are equal, false otherwise.
	 */
	public boolean equals(SortableJobKey other)
	{
		return other._jobID == _jobID;
	}
	
	/**
	 * Java requires that if you overload equals, you have to do it
	 * for the method signature equals(Object).
	 */
	public boolean equals(Object other) 
	{
		/* If the other object IS a sortable job key, call through
		 * to out other equals method.
		 */
		if (other instanceof SortableJobKey)
			return equals((SortableJobKey)other);
		
		/* Otherwise, they aren't equal */
		return false;
	}
	
	/**
	 * This method is used to order to SortableJobKey's.
	 * 
	 * @param other The other job key to compare against.
	 * @return If "this" object is less than the "other" object, return 
	 * a negative integer, if greater, return a positive integer, if equal,
	 * return 0.
	 */
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