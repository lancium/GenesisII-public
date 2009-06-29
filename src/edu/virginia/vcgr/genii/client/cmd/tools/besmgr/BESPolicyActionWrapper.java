package edu.virginia.vcgr.genii.client.cmd.tools.besmgr;

import edu.virginia.vcgr.genii.container.bes.BESPolicyActions;

public enum BESPolicyActionWrapper
{
	NOACTION(BESPolicyActions.NOACTION, "Do Nothing"),
	SUSPEND(BESPolicyActions.SUSPEND, "Suspend Jobs"),
	SUSPEND_OR_KILL(BESPolicyActions.SUSPENDORKILL, "Suspend or Kill Jobs"),
	KILL(BESPolicyActions.KILL, "Kill Jobs");
	
	private String _description;
	private BESPolicyActions _action;
	
	private BESPolicyActionWrapper(BESPolicyActions action, String description)
	{
		_action = action;
		_description = description;
	}
	
	@Override
	public String toString()
	{
		return _description;
	}
	
	public BESPolicyActions action()
	{
		return _action;
	}
	
	static public BESPolicyActionWrapper wrap(BESPolicyActions action)
	{
		if (action == BESPolicyActions.NOACTION)
			return NOACTION;
		else if (action == BESPolicyActions.SUSPEND)
			return SUSPEND;
		else if (action == BESPolicyActions.SUSPENDORKILL)
			return SUSPEND_OR_KILL;
		else if (action == BESPolicyActions.KILL)
			return KILL;
		else
			throw new IllegalArgumentException(String.format(
				"Don't know how to wrap BES policy action \"%s\".", action));
	}
}