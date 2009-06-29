package edu.virginia.vcgr.genii.client.cmd.tools.besmgr;

import edu.virginia.vcgr.genii.container.bes.BESPolicy;

public class ManagementData
{
	private boolean _storedAcceptingNewActivities;
	private BESPolicy _storedPolicy;
	private Integer _storedThreshold = null;
	
	public ManagementData(BESPolicy policy,
		boolean isAcceptingNewActivities,
		Integer threshold)
	{
		_storedAcceptingNewActivities = isAcceptingNewActivities;
		_storedPolicy = policy;
		_storedThreshold = threshold;
	}
	
	final public boolean isAcceptingNewActivities()
	{
		return _storedAcceptingNewActivities;
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