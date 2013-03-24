package edu.virginia.vcgr.xscript;

import javax.script.ScriptException;

public interface ParseStatement
{
	public Object evaluate(XScriptContext context) throws ScriptException, EarlyExitException, ReturnFromFunctionException;
}