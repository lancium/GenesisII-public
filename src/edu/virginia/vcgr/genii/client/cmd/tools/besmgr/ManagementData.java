package edu.virginia.vcgr.genii.client.cmd.tools.besmgr;

import edu.virginia.vcgr.genii.container.bes.BESPolicy;

public class ManagementData
{
	private BESPolicy _storedPolicy;
	private Integer _storedThreshold = null;
	private boolean _isAcceptingActivities;

	public ManagementData(BESPolicy policy, Integer threshold, boolean isAcceptingActivities)
	{
		_isAcceptingActivities = isAcceptingActivities;
		_storedPolicy = policy;
		_storedThreshold = threshold;
	}

	final public boolean isAcceptingActivities()
	{
		return _isAcceptingActivities;
	}

	final public BESPolicy policy()
	{
		return _storedPolicy;
	}

	final public Integer threshold()
	{
		return _storedThreshold;
	}
}