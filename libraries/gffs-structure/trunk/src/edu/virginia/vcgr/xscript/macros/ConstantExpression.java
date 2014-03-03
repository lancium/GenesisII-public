package edu.virginia.vcgr.xscript.macros;

import javax.script.ScriptContext;

public class ConstantExpression implements MacroExpression
{
	private String _constant;

	public ConstantExpression(String constant)
	{
		_constant = constant;
	}

	@Override
	public String toString(ScriptContext variables)
	{
		return _constant;
	}
}