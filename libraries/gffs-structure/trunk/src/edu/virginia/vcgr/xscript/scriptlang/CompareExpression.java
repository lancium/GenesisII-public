package edu.virginia.vcgr.xscript.scriptlang;

import javax.script.ScriptException;

import edu.virginia.vcgr.xscript.XScriptContext;
import edu.virginia.vcgr.xscript.XScriptParser;
import edu.virginia.vcgr.xscript.macros.MacroReplacer;

public class CompareExpression extends AbstractConditionExpression
{
	private String _isNumeric;
	private String _arg1;
	private String _arg2;
	private String _comparison;

	public CompareExpression(String isNumeric, String arg1, String arg2, String comparison)
	{
		_isNumeric = isNumeric;
		_arg1 = arg1;
		_arg2 = arg2;
		_comparison = comparison;
	}

	@Override
	public boolean evaluateCondition(XScriptContext context) throws ScriptException
	{
		boolean isNumeric = XScriptParser.getBoolean(context, _isNumeric);
		String arg1 = MacroReplacer.replaceMacros(context, _arg1);
		String arg2 = MacroReplacer.replaceMacros(context, _arg2);
		String comparison = MacroReplacer.replaceMacros(context, _comparison);

		if (!comparison.equals("lt") && !comparison.equals("le") && !comparison.equals("eq") && !comparison.equals("ge")
			&& !comparison.equals("gt"))
			throw new ScriptException(String.format("\"comparison\" attribute of <{%s}:%s> element must "
				+ "be one of [lt, le, eq, ge, gt].", XScriptContext.XSCRIPT_NS, "compare"));

		int result;
		if (isNumeric)
			result = compareNumeric(arg1, arg2);
		else
			result = arg1.compareTo(arg2);

		if (comparison.equals("lt"))
			return result < 0;
		else if (comparison.equals("le"))
			return result <= 0;
		else if (comparison.equals("eq"))
			return result == 0;
		else if (comparison.equals("ge"))
			return result >= 0;
		else
			return result > 0;
	}

	static private int compareNumeric(String one, String two)
	{
		if (one.contains(".") || two.contains("."))
			return Double.valueOf(one).compareTo(Double.valueOf(two));

		return Long.valueOf(one).compareTo(Long.valueOf(two));
	}
}