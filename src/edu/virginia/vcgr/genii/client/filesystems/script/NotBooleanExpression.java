package edu.virginia.vcgr.genii.client.filesystems.script;

import edu.virginia.vcgr.genii.client.filesystems.FilesystemUsageInformation;

class NotBooleanExpression implements BooleanExpression
{
	private BooleanExpression _subExpr;
	
	NotBooleanExpression(BooleanExpression subExpr)
	{
		_subExpr = subExpr;
	}
	
	@Override
	final public boolean evaluate(FilesystemUsageInformation usageInformation)
	{
		return !_subExpr.evaluate(usageInformation);
	}
	
	@Override
	final public String toString()
	{
		return String.format("!%s", _subExpr);
	}
}
