package edu.virginia.vcgr.xscript.macros;

import javax.script.ScriptContext;

public class LookupExpression implements MacroExpression
{
	private MacroExpression _subExpression;
	
	public LookupExpression(MacroExpression subExpression)
	{
		_subExpression = subExpression;
	}
	
	@Override
	public String toString(ScriptContext variables)
	{
		String subExpression = _subExpression.toString(variables);
		Object result = variables.getAttribute(subExpression);
		if (result == null)
			result = subExpression;
		
		return result.toString();
	}
}