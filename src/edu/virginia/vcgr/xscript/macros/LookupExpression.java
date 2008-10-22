package edu.virginia.vcgr.xscript.macros;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptContext;

public class LookupExpression implements MacroExpression
{
	static private Pattern ARRAY_PATTERN = Pattern.compile(
		"^([^\\[]+)\\[(\\d+)]$");
	
	private MacroExpression _subExpression;
	
	public LookupExpression(MacroExpression subExpression)
	{
		_subExpression = subExpression;
	}
	
	@Override
	public String toString(ScriptContext variables)
	{
		String variable;
		int index;
		boolean wasIndexed = false;
		
		String subExpression = _subExpression.toString(variables);
		Matcher matcher = ARRAY_PATTERN.matcher(subExpression);
		
		if (matcher.matches())
		{
			wasIndexed = true;
			variable = matcher.group(1);
			index = Integer.parseInt(matcher.group(2));
		} else
		{
			variable = subExpression;
			index = 0;
		}
		
		Object result = variables.getAttribute(variable);
		if (result == null)
			result = "";
		
		if (result.getClass().isArray())
		{
			Object[] array = (Object[])result;
			if (!wasIndexed)
				return Integer.toString(array.length);
			if (index < 0 || index >= array.length)
				return "";
			
			return array[index].toString();
		} else
		{
			if (index == 0)
				return result.toString();
			return "";
		}
	}
}