package edu.virginia.vcgr.xscript.scriptlang;

import javax.script.ScriptException;

import edu.virginia.vcgr.xscript.EarlyExitException;
import edu.virginia.vcgr.xscript.ParseStatement;
import edu.virginia.vcgr.xscript.ReturnFromFunctionException;
import edu.virginia.vcgr.xscript.XScriptContext;
import edu.virginia.vcgr.xscript.macros.MacroReplacer;

public class ForStatement implements ParseStatement
{
	private String _paramName;
	private String _initialValue;
	private String _inclusiveLimit;
	private String _exclusiveLimit;
	private String _incrementValue;
	private ParseStatement _innerStatement;

	public ForStatement(String paramName, String initialValue, String inclusiveLimit, String exclusiveLimit,
		String incrementValue, ParseStatement innerStatement)
	{
		_paramName = paramName;
		_initialValue = initialValue;
		_inclusiveLimit = inclusiveLimit;
		_exclusiveLimit = exclusiveLimit;
		_incrementValue = incrementValue;
		_innerStatement = innerStatement;
	}

	@Override
	public Object evaluate(XScriptContext context) throws ScriptException, EarlyExitException, ReturnFromFunctionException
	{
		String paramName = MacroReplacer.replaceMacros(context, _paramName);
		int initialValue = Integer.parseInt(MacroReplacer.replaceMacros(context, _initialValue));
		int incrementValue = Integer.parseInt(MacroReplacer.replaceMacros(context, _incrementValue));
		int limit;
		if (_inclusiveLimit != null)
			limit = Integer.parseInt(MacroReplacer.replaceMacros(context, _inclusiveLimit)) + 1;
		else
			limit = Integer.parseInt(MacroReplacer.replaceMacros(context, _exclusiveLimit));

		Object result = null;
		for (int lcv = initialValue; lcv < limit; lcv += incrementValue) {
			context.setAttribute(paramName, lcv);
			result = _innerStatement.evaluate(context);
		}

		return result;
	}
}