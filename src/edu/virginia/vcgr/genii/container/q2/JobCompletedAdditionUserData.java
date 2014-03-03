package edu.virginia.vcgr.genii.container.q2;

import javax.xml.bind.annotation.XmlAttribute;

import edu.virginia.vcgr.genii.client.wsrf.wsn.AdditionalUserData;

public class JobCompletedAdditionUserData extends AdditionalUserData
{
	static final long serialVersionUID = 0L;

	@XmlAttribute(name = "job-id", required = true)
	private long _jobID;

	@SuppressWarnings("unused")
	private JobCompletedAdditionUserData()
	{
	}

	public JobCompletedAdditionUserData(long jobID)
	{
		_jobID = jobID;
	}

	final public long jobID()
	{
		return _jobID;
	}
}