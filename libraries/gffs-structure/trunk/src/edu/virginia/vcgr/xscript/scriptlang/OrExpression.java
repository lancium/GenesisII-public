package edu.virginia.vcgr.xscript.scriptlang;

public class OrExpression extends MultiAbstractConditionExpression
{
	@Override
	protected boolean combine(boolean previous, boolean next)
	{
		return previous || next;
	}
}