package edu.virginia.vcgr.xscript;

import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

public class XScript extends CompiledScript
{
	private XScriptEngine _engine;
	private ParseStatement _program;
	
	XScript(XScriptEngine engine, ParseStatement program)
	{
		_engine = engine;
		_program = program;
	}
	
	@Override
	public Object eval(ScriptContext context) throws ScriptException
	{
		try
		{
			return _program.evaluate(
				(XScriptContext)context);
		} catch (EarlyExitException e)
		{
			return e.getExitCode();
		} catch (ReturnFromFunctionException e)
		{
			return e.getResult();
		}
	}

	@Override
	public ScriptEngine getEngine()
	{
		return _engine;
	}
}