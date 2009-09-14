package edu.virginia.vcgr.genii.container.bes.jsdl.personality.common;

public class JobUnderstandingContext
{
	private String _requiredOGRSHVersion;
	
	private ResourceConstraints _resourceConstraints;
	
	public JobUnderstandingContext(String requiredOGRSHVersion,
		ResourceConstraints resourceConstraints)
	{
		_requiredOGRSHVersion = requiredOGRSHVersion; 

		_resourceConstraints = resourceConstraints;
	}
	
	final public String getRequiredOGRSHVersion()
	{
		return _requiredOGRSHVersion;
	}
	
	final public ResourceConstraints getResourceConstraints()
	{
		return _resourceConstraints;
	}
}