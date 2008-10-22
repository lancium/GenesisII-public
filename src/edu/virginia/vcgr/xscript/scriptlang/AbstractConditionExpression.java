package edu.virginia.vcgr.xscript.scriptlang;

import javax.script.ScriptException;

import edu.virginia.vcgr.xscript.EarlyExitException;
import edu.virginia.vcgr.xscript.ReturnFromFunctionException;
import edu.virginia.vcgr.xscript.XScriptContext;

public abstract class AbstractConditionExpression implements ConditionExpression
{
	@Override
	public Object evaluate(XScriptContext context) throws ScriptException,
			EarlyExitException, ReturnFromFunctionException
	{
		return new Boolean(evaluateCondition(context));
	}
}