package edu.virginia.vcgr.xscript.macros;

import javax.script.ScriptContext;

public class ConcatenateExpression implements MacroExpression
{
	private MacroExpression _left;
	private MacroExpression _right;

	public ConcatenateExpression(MacroExpression left, MacroExpression right)
	{
		_left = left;
		_right = right;
	}

	@Override
	public String toString(ScriptContext variables)
	{
		return _left.toString(variables) + _right.toString(variables);
	}
}