package edu.virginia.vcgr.xscript.scriptlang;

import java.util.Collection;
import java.util.LinkedList;

import javax.script.ScriptException;

import edu.virginia.vcgr.xscript.EarlyExitException;
import edu.virginia.vcgr.xscript.ParseStatement;
import edu.virginia.vcgr.xscript.ReturnFromFunctionException;
import edu.virginia.vcgr.xscript.XScriptContext;

public class BlockStatement implements ParseStatement
{
	private Collection<ParseStatement> _statements = new LinkedList<ParseStatement>();

	public void addStatement(ParseStatement stmt)
	{
		_statements.add(stmt);
	}

	@Override
	public Object evaluate(XScriptContext context) throws ScriptException, EarlyExitException, ReturnFromFunctionException
	{
		Object result = null;

		for (ParseStatement stmt : _statements) {
			result = stmt.evaluate(context);
		}

		return result;
	}
}