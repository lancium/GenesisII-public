package edu.virginia.vcgr.xscript.scriptlang;

import java.util.regex.Pattern;

import javax.script.ScriptException;

import edu.virginia.vcgr.xscript.XScriptContext;
import edu.virginia.vcgr.xscript.macros.MacroReplacer;

public class MatchesExpression extends AbstractConditionExpression
{
	private String _string;
	private String _pattern;
	
	public MatchesExpression(String string, String pattern)
	{
		_string = string;
		_pattern = pattern;
	}
	
	@Override
	public boolean evaluateCondition(XScriptContext context)
			throws ScriptException
	{
		Pattern pattern = Pattern.compile(
			MacroReplacer.replaceMacros(context, _pattern));
		return pattern.matcher(MacroReplacer.replaceMacros(
			context, _string)).matches();
	}
}