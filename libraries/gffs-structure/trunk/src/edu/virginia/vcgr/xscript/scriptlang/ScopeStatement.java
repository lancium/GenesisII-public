package edu.virginia.vcgr.xscript.scriptlang;

import javax.script.ScriptException;

import edu.virginia.vcgr.xscript.EarlyExitException;
import edu.virginia.vcgr.xscript.ParseStatement;
import edu.virginia.vcgr.xscript.ReturnFromFunctionException;
import edu.virginia.vcgr.xscript.XScriptContext;

public class ScopeStatement implements ParseStatement
{
	private ParseStatement _stmt;

	public ScopeStatement(ParseStatement stmt)
	{
		_stmt = stmt;
	}

	@Override
	public Object evaluate(XScriptContext context) throws ScriptException, EarlyExitException, ReturnFromFunctionException
	{
		context.push();
		try {
			return _stmt.evaluate(context);
		} finally {
			context.pop();
		}
	}
}