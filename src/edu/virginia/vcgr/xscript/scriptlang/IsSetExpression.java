package edu.virginia.vcgr.xscript.scriptlang;

import javax.script.ScriptException;

import edu.virginia.vcgr.xscript.XScriptContext;
import edu.virginia.vcgr.xscript.macros.MacroReplacer;

public class IsSetExpression extends AbstractConditionExpression
{
	private String _property;

	public IsSetExpression(String property)
	{
		_property = property;
	}

	@Override
	public boolean evaluateCondition(XScriptContext context) throws ScriptException
	{
		Object val = context.getAttribute(MacroReplacer.replaceMacros(context, _property));
		if (val == null)
			return false;

		return true;
	}
}