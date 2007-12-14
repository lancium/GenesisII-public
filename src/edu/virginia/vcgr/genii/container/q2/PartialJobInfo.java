package edu.virginia.vcgr.genii.container.q2;

import java.util.Collection;
import java.util.Date;

import edu.virginia.vcgr.genii.client.security.gamlauthz.identity.Identity;

/**
 * This class is poorly named because it doesn't serve a concise purpose.  
 * Essentially, it is a data structure to bundle together some information 
 * that we get occassionally from the database.  Basically, it is information
 * about who owns a job and what the start/finish times were for the
 * job.
 * 
 * @author mmm2a
 */
public class PartialJobInfo
{
	private Collection<Identity> _owners;
	private Date _startTime;
	private Date _finishTime;
	
	public PartialJobInfo(Collection<Identity> owners, 
		Date startTime, Date finishTime)
	{
		_owners = owners;
		_startTime = startTime;
		_finishTime = finishTime;
	}
	
	public Collection<Identity> getOwners()
	{
		return _owners;
	}
	
	public Date getStartTime()
	{
		return _startTime;
	}
	
	public Date getFinishTime()
	{
		return _finishTime;
	}
}