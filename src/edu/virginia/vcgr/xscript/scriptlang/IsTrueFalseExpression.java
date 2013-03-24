package edu.virginia.vcgr.xscript.scriptlang;

import javax.script.ScriptException;

import edu.virginia.vcgr.xscript.XScriptContext;
import edu.virginia.vcgr.xscript.XScriptParser;

public class IsTrueFalseExpression extends AbstractConditionExpression
{
	private String _test;
	private boolean _isTrue;

	public IsTrueFalseExpression(String test, boolean isTrue)
	{
		_test = test;
		_isTrue = isTrue;
	}

	@Override
	public boolean evaluateCondition(XScriptContext context) throws ScriptException
	{
		if (_isTrue)
			return XScriptParser.getBoolean(context, _test);
		else
			return !XScriptParser.getBoolean(context, _test);
	}
}