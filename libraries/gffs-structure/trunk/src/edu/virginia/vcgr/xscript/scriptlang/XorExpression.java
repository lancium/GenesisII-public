package edu.virginia.vcgr.xscript.scriptlang;

public class XorExpression extends MultiAbstractConditionExpression
{
	@Override
	protected boolean combine(boolean previous, boolean next)
	{
		return previous ^ next;
	}
}