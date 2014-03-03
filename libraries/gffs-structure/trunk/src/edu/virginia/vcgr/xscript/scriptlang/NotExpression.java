package edu.virginia.vcgr.xscript.scriptlang;

import javax.script.ScriptException;

import edu.virginia.vcgr.xscript.XScriptContext;

public class NotExpression extends AbstractConditionExpression
{
	private ConditionExpression _subExpression;

	public NotExpression(ConditionExpression subExpression)
	{
		_subExpression = subExpression;
	}

	@Override
	public boolean evaluateCondition(XScriptContext context) throws ScriptException
	{
		return !_subExpression.evaluateCondition(context);
	}
}