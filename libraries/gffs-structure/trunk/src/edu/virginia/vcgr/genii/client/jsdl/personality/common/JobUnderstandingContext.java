package edu.virginia.vcgr.genii.client.jsdl.personality.common;

import java.io.File;

public class JobUnderstandingContext
{
	private File _fuseMountPoint;

	private ResourceConstraints _resourceConstraints;
	private String _jobName;

	public JobUnderstandingContext(File fuseMountPoint, ResourceConstraints resourceConstraints, String jobName)
	{
		_fuseMountPoint = fuseMountPoint;

		_resourceConstraints = resourceConstraints;
		_jobName = jobName;
	}

	final public File getFuseMountPoint()
	{
		return _fuseMountPoint;
	}

	final public ResourceConstraints getResourceConstraints()
	{
		return _resourceConstraints;
	}
	final public String getJobName()
	{
		return _jobName;
	}
}