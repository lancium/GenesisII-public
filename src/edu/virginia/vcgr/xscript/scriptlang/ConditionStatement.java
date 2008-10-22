package edu.virginia.vcgr.xscript.scriptlang;

import javax.script.ScriptException;

import edu.virginia.vcgr.xscript.EarlyExitException;
import edu.virginia.vcgr.xscript.ParseStatement;
import edu.virginia.vcgr.xscript.ReturnFromFunctionException;
import edu.virginia.vcgr.xscript.XScriptContext;
import edu.virginia.vcgr.xscript.macros.MacroReplacer;

public class ConditionStatement implements ParseStatement
{
	private String _propertyname;
	private ConditionExpression _expression;
	
	public ConditionStatement(String propertyName, 
		ConditionExpression expression)
	{
		_propertyname = propertyName;
		_expression = expression;
	}
	
	@Override
	public Object evaluate(XScriptContext context) throws ScriptException,
			EarlyExitException, ReturnFromFunctionException
	{
		Boolean value = new Boolean(
			_expression.evaluateCondition(context));
		context.setAttribute(
			MacroReplacer.replaceMacros(context, _propertyname),
			value);
		return value;
	}
}