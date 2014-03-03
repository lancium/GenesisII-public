package edu.virginia.vcgr.xscript.scriptlang;

import java.util.Collection;
import java.util.LinkedList;
import java.util.regex.Pattern;

import javax.script.ScriptException;

import edu.virginia.vcgr.xscript.EarlyExitException;
import edu.virginia.vcgr.xscript.ParseStatement;
import edu.virginia.vcgr.xscript.ReturnFromFunctionException;
import edu.virginia.vcgr.xscript.XScriptContext;
import edu.virginia.vcgr.xscript.macros.MacroReplacer;

public class SwitchStatement implements ParseStatement
{
	static private class Case
	{
		private String _casePattern;
		private ParseStatement _innerStatement;

		private Case(String casePattern, ParseStatement innerStatement)
		{
			_casePattern = casePattern;
			_innerStatement = innerStatement;
		}
	}

	private String _value;
	private Collection<Case> _cases = new LinkedList<Case>();
	private ParseStatement _defaultStatement = null;

	public SwitchStatement(String value)
	{
		_value = value;
	}

	public void addCase(String casePattern, ParseStatement innerStatement)
	{
		_cases.add(new Case(casePattern, innerStatement));
	}

	public void setDefault(ParseStatement innerStatement) throws ScriptException
	{
		if (_defaultStatement != null)
			throw new ScriptException("A case statement can only have one default statement.");

		_defaultStatement = innerStatement;
	}

	@Override
	public Object evaluate(XScriptContext context) throws ScriptException, EarlyExitException, ReturnFromFunctionException
	{
		String value = MacroReplacer.replaceMacros(context, _value);
		for (Case c : _cases) {
			if (Pattern.matches(MacroReplacer.replaceMacros(context, c._casePattern), value))
				return c._innerStatement.evaluate(context);
		}

		if (_defaultStatement != null)
			return _defaultStatement.evaluate(context);

		return Boolean.FALSE;
	}
}