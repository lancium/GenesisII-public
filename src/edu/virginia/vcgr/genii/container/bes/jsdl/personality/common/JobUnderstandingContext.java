package edu.virginia.vcgr.genii.container.bes.jsdl.personality.common;

import java.io.File;

public class JobUnderstandingContext
{
	private File _fuseMountPoint;

	private ResourceConstraints _resourceConstraints;

	public JobUnderstandingContext(File fuseMountPoint, ResourceConstraints resourceConstraints)
	{
		_fuseMountPoint = fuseMountPoint;

		_resourceConstraints = resourceConstraints;
	}

	final public File getFuseMountPoint()
	{
		return _fuseMountPoint;
	}

	final public ResourceConstraints getResourceConstraints()
	{
		return _resourceConstraints;
	}
}