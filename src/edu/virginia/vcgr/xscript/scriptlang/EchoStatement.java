package edu.virginia.vcgr.xscript.scriptlang;

import java.io.IOException;

import javax.script.ScriptException;

import edu.virginia.vcgr.xscript.EarlyExitException;
import edu.virginia.vcgr.xscript.ParseStatement;
import edu.virginia.vcgr.xscript.ReturnFromFunctionException;
import edu.virginia.vcgr.xscript.XScriptContext;
import edu.virginia.vcgr.xscript.macros.MacroReplacer;

public class EchoStatement implements ParseStatement
{
	private String _message;

	EchoStatement(String message)
	{
		_message = message;
	}

	@Override
	public Object evaluate(XScriptContext context) throws ScriptException, EarlyExitException, ReturnFromFunctionException
	{
		try {
			StringBuilder builder = new StringBuilder();
			builder.append(MacroReplacer.replaceMacros(context, _message));
			builder.append("\n");
			String ret = builder.toString();
			context.getWriter().append(ret);
			context.getWriter().flush();
			return ret;
		} catch (IOException ioe) {
			throw new ScriptException(ioe);
		}
	}
}