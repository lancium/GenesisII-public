package edu.virginia.vcgr.xscript.scriptlang;

import java.util.Collection;
import java.util.LinkedList;

import javax.script.ScriptException;

import edu.virginia.vcgr.xscript.EarlyExitException;
import edu.virginia.vcgr.xscript.ParseStatement;
import edu.virginia.vcgr.xscript.ReturnFromFunctionException;
import edu.virginia.vcgr.xscript.XScriptContext;

public class ScopeStatement implements ParseStatement
{
	private Collection<ParseStatement> _statements = 
		new LinkedList<ParseStatement>();
	
	public void addStatement(ParseStatement stmt)
	{
		_statements.add(stmt);
	}
	
	@Override
	public Object evaluate(XScriptContext context) throws ScriptException,
			EarlyExitException, ReturnFromFunctionException
	{
		Object result = null;
		
		context.push();
		try
		{
			for (ParseStatement stmt : _statements)
			{
				result = stmt.evaluate(context);
			}
		}
		catch (ReturnFromFunctionException rffe)
		{
			result = rffe.getResult();
		}
		finally
		{
			context.pop();
		}
		
		return result;
	}
}