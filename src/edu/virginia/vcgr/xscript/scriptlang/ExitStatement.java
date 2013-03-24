package edu.virginia.vcgr.xscript.scriptlang;

import javax.script.ScriptException;

import edu.virginia.vcgr.xscript.EarlyExitException;
import edu.virginia.vcgr.xscript.ParseStatement;
import edu.virginia.vcgr.xscript.ReturnFromFunctionException;
import edu.virginia.vcgr.xscript.XScriptContext;
import edu.virginia.vcgr.xscript.macros.MacroReplacer;

public class ExitStatement implements ParseStatement
{
	private String _exitCode;

	public ExitStatement(String exitCode)
	{
		_exitCode = exitCode;
	}

	@Override
	public Object evaluate(XScriptContext context) throws ScriptException, EarlyExitException, ReturnFromFunctionException
	{
		int exitCode = Integer.parseInt(MacroReplacer.replaceMacros(context, _exitCode));
		throw new EarlyExitException(exitCode);
	}
}