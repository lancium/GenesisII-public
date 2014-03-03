package edu.virginia.vcgr.xscript.scriptlang;

import javax.script.ScriptException;

import edu.virginia.vcgr.xscript.EarlyExitException;
import edu.virginia.vcgr.xscript.ParseStatement;
import edu.virginia.vcgr.xscript.ReturnFromFunctionException;
import edu.virginia.vcgr.xscript.XScriptContext;
import edu.virginia.vcgr.xscript.macros.MacroReplacer;

public class ReturnStatement implements ParseStatement
{
	private String _property;
	private String _value;
	private ParseStatement _statement;

	public ReturnStatement(String property, String value, ParseStatement stmt)
	{
		_property = property;
		_value = value;
		_statement = stmt;
	}

	@Override
	public Object evaluate(XScriptContext context) throws ScriptException, EarlyExitException, ReturnFromFunctionException
	{
		Object ret;

		if (_property != null)
			ret = context.getAttribute(MacroReplacer.replaceMacros(context, _property));
		else if (_value != null)
			ret = MacroReplacer.replaceMacros(context, _value);
		else
			ret = _statement.evaluate(context);

		throw new ReturnFromFunctionException(ret);
	}
}