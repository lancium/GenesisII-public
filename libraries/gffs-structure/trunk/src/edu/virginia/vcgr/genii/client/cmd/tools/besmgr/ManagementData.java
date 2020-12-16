package edu.virginia.vcgr.genii.client.cmd.tools.besmgr;

public class ManagementData
{
	private Integer _storedThreshold = null;
	private boolean _isAcceptingActivities;

	public ManagementData(Integer threshold, boolean isAcceptingActivities)
	{
		_isAcceptingActivities = isAcceptingActivities;
		_storedThreshold = threshold;
	}

	final public boolean isAcceptingActivities()
	{
		return _isAcceptingActivities;
	}

	final public Integer threshold()
	{
		return _storedThreshold;
	}
}