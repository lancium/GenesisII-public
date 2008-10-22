package edu.virginia.vcgr.xscript.scriptlang;

import javax.script.ScriptException;

import edu.virginia.vcgr.xscript.EarlyExitException;
import edu.virginia.vcgr.xscript.ParseStatement;
import edu.virginia.vcgr.xscript.ReturnFromFunctionException;
import edu.virginia.vcgr.xscript.XScriptContext;
import edu.virginia.vcgr.xscript.macros.MacroReplacer;

public class DefaultStatement implements ParseStatement
{
	private String _name;
	private String _value;
	
	public DefaultStatement(String name, String value)
	{
		_name = name;
		_value = value;
	}
	
	@Override
	public Object evaluate(XScriptContext context) throws ScriptException,
			EarlyExitException, ReturnFromFunctionException
	{
		String name = MacroReplacer.replaceMacros(context, _name);
		
		Object val = context.getAttribute(name);
		if (val == null)
		{
			val = MacroReplacer.replaceMacros(context, _value);
			context.setAttribute(name, val);
		}
		
		return val;
	}
}