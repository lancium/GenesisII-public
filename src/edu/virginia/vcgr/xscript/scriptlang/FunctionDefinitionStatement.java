package edu.virginia.vcgr.xscript.scriptlang;

import javax.script.ScriptContext;
import javax.script.ScriptException;

import edu.virginia.vcgr.xscript.EarlyExitException;
import edu.virginia.vcgr.xscript.ParseStatement;
import edu.virginia.vcgr.xscript.ReturnFromFunctionException;
import edu.virginia.vcgr.xscript.XScriptContext;

public class FunctionDefinitionStatement implements ParseStatement
{
	private String _functionName;
	private ParseStatement _functionBody;

	public FunctionDefinitionStatement(String functionName, ParseStatement functionBody)
	{
		_functionName = functionName;
		_functionBody = functionBody;
	}

	@Override
	public Object evaluate(XScriptContext context) throws ScriptException, EarlyExitException, ReturnFromFunctionException
	{
		context.setAttribute(_functionName, _functionBody, ScriptContext.GLOBAL_SCOPE);
		return _functionName;
	}
}