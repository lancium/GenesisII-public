package edu.virginia.vcgr.xscript.scriptlang;

import java.util.Collection;
import java.util.LinkedList;

import javax.script.ScriptException;

import edu.virginia.vcgr.xscript.XScriptContext;

public abstract class MultiAbstractConditionExpression 
	extends AbstractConditionExpression
{
	private Collection<ConditionExpression> _contents =
		new LinkedList<ConditionExpression>();
	
	public void addConditionExpression(ConditionExpression expression)
	{
		_contents.add(expression);
	}
	
	protected abstract boolean combine(boolean previous, boolean next);
	
	@Override
	public boolean evaluateCondition(XScriptContext context)
			throws ScriptException
	{
		if (_contents.size() == 0)
			return false;
		
		Boolean previous = null;
		for (ConditionExpression expr : _contents)
		{
			if (previous == null)
				previous = new Boolean(
					expr.evaluateCondition(context));
			else
			{
				previous = new Boolean(combine(previous.booleanValue(),
					expr.evaluateCondition(context)));
			}
		}
		
		return previous.booleanValue();
	}
}