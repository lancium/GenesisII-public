package edu.virginia.vcgr.genii.container.q2;

/**
 * A simple data structure to store job and bes ID's together. This is used by code that needs to
 * associate the two entities together into one data structure (such as code that will check the
 * status of the job, or possibly kill a job).
 * 
 * @author mmm2a
 */
public class JobCommunicationInfo
{
	private long _jobID;
	private long _besID;

	public JobCommunicationInfo(long jobID, long besID)
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