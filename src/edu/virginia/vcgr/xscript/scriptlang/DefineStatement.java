package edu.virginia.vcgr.xscript.scriptlang;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptException;

import edu.virginia.vcgr.xscript.EarlyExitException;
import edu.virginia.vcgr.xscript.ParseStatement;
import edu.virginia.vcgr.xscript.ReturnFromFunctionException;
import edu.virginia.vcgr.xscript.XScriptContext;
import edu.virginia.vcgr.xscript.XScriptParser;
import edu.virginia.vcgr.xscript.macros.MacroReplacer;

public class DefineStatement implements ParseStatement
{
	private String _variable;
	private String _source;
	private Pattern _pattern;
	private String _replacement;
	private String _isGlobal;
	
	DefineStatement(String variable, String source,
		Pattern pattern, String replacement, String isGlobal)
	{
		_variable = variable;
		_source = source;
		_pattern = pattern;
		_replacement = replacement;
		_isGlobal = isGlobal;
	}
	
	@Override
	public Object evaluate(XScriptContext context) throws ScriptException,
			EarlyExitException, ReturnFromFunctionException
	{
		String variable = MacroReplacer.replaceMacros(context, _variable);
		String source = MacroReplacer.replaceMacros(context, _source);
		String result;
		
		if (_pattern != null)
		{
			Matcher matcher = _pattern.matcher(source);
			if (XScriptParser.getBoolean(context, _isGlobal))
				result = matcher.replaceAll(_replacement);
			else
				result = matcher.replaceFirst(_replacement);
		} else
			result = source;
		
		context.setAttribute(variable, result);
		return result;
	}
}