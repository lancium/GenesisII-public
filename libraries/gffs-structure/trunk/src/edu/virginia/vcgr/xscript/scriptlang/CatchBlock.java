package edu.virginia.vcgr.xscript.scriptlang;

import javax.script.ScriptException;

import edu.virginia.vcgr.xscript.EarlyExitException;
import edu.virginia.vcgr.xscript.ParseStatement;
import edu.virginia.vcgr.xscript.ReturnFromFunctionException;
import edu.virginia.vcgr.xscript.XScriptContext;
import edu.virginia.vcgr.xscript.macros.MacroReplacer;

public class CatchBlock implements ParseStatement
{
	private String _property;
	private String _message;
	private ParseStatement _statement;

	public CatchBlock(ParseStatement statement, String property, String message)
	{
		_property = property;
		_message = message;
		_statement = statement;
	}

	@Override
	public Object evaluate(XScriptContext context) throws ScriptException, EarlyExitException, ReturnFromFunctionException
	{
		Throwable e = context.getLastException();
		if (e != null) {
			if (_property != null)
				context.setAttribute(MacroReplacer.replaceMacros(context, _property), e);
			if (_message != null)
				context.setAttribute(MacroReplacer.replaceMacros(context, _message), e.getLocalizedMessage());
		}

		return _statement.evaluate(context);
	}
}