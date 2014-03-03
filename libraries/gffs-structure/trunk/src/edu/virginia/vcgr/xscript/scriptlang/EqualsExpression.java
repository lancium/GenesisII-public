package edu.virginia.vcgr.xscript.scriptlang;

import javax.script.ScriptException;

import edu.virginia.vcgr.xscript.XScriptContext;
import edu.virginia.vcgr.xscript.XScriptParser;
import edu.virginia.vcgr.xscript.macros.MacroReplacer;

public class EqualsExpression extends AbstractConditionExpression
{
	private String _isCaseSensitive;
	private String _arg1;
	private String _arg2;

	public EqualsExpression(String arg1, String arg2, String isCaseString)
	{
		_arg1 = arg1;
		_arg2 = arg2;
		_isCaseSensitive = isCaseString;
	}

	@Override
	public boolean evaluateCondition(XScriptContext context) throws ScriptException
	{
		String arg1 = MacroReplacer.replaceMacros(context, _arg1);
		String arg2 = MacroReplacer.replaceMacros(context, _arg2);

		if (XScriptParser.getBoolean(context, _isCaseSensitive))
			return arg1.equals(arg2);

		return arg1.equalsIgnoreCase(arg2);
	}
}