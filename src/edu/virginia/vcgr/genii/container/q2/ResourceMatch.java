package edu.virginia.vcgr.genii.container.q2;

/**
 * A simple data structure which binds a job id to the bes container id
 * that it should be run on.
 * 
 * @author mmm2a
 */
public class ResourceMatch
{
	private long _jobID;
	private long _besID;
	
	public ResourceMatch(long jobID, long besID)
	{
		_jobID = jobID;
		_besID = besID;
	}
	
	public long getJobID()
	{
		return _jobID;
	}
	
	public long getBESID()
	{
		return _besID;
	}
}