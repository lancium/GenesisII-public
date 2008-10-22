package edu.virginia.vcgr.xscript.scriptlang;

import javax.script.ScriptException;

import edu.virginia.vcgr.xscript.EarlyExitException;
import edu.virginia.vcgr.xscript.ParseStatement;
import edu.virginia.vcgr.xscript.ReturnFromFunctionException;
import edu.virginia.vcgr.xscript.XScriptContext;
import edu.virginia.vcgr.xscript.XScriptParser;
import edu.virginia.vcgr.xscript.macros.MacroReplacer;

public class IfStatement implements ParseStatement
{
	private String _test;
	private ParseStatement _thenStmt;
	private ParseStatement _elseStmt;
	
	public IfStatement(String test, ParseStatement thenStmt, 
		ParseStatement elseStmt)
	{
		_test = test;
		_thenStmt = thenStmt;
		_elseStmt = elseStmt;
	}
	
	@Override
	public Object evaluate(XScriptContext context) throws ScriptException,
			EarlyExitException, ReturnFromFunctionException
	{
		boolean test;
		
		String property = MacroReplacer.replaceMacros(context, _test);
		Object value = context.getAttribute(property);
		if (value == null)
			test = false;
		else
		{
			if (value instanceof Boolean)
				test = ((Boolean)value).booleanValue();
			else
				test = XScriptParser.getBoolean(context, value.toString());
		}
		
		if (test)
			return _thenStmt.evaluate(context);
		
		if (_elseStmt != null)
			return _elseStmt.evaluate(context);
		
		return null;
	}
}