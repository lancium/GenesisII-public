package edu.virginia.vcgr.xscript.scriptlang;

import java.util.ArrayList;
import java.util.Collection;

import javax.script.ScriptContext;
import javax.script.ScriptException;

import edu.virginia.vcgr.xscript.EarlyExitException;
import edu.virginia.vcgr.xscript.ParseStatement;
import edu.virginia.vcgr.xscript.ReturnFromFunctionException;
import edu.virginia.vcgr.xscript.XScriptContext;
import edu.virginia.vcgr.xscript.macros.MacroReplacer;

public class CallStatement implements ParseStatement
{
	private String _functionName;
	private String _property;
	private Collection<ParseStatement> _parameters;
	
	public CallStatement(String functionName, String property)
	{
		_functionName = functionName;
		_property = property;
		_parameters = new ArrayList<ParseStatement>();
	}
	
	public void addParameter(ParseStatement stmt)
	{
		_parameters.add(stmt);
	}
	
	@Override
	public Object evaluate(XScriptContext context) throws ScriptException,
			EarlyExitException, ReturnFromFunctionException
	{
		Object functionObj = context.getAttribute(
			MacroReplacer.replaceMacros(context, _functionName),
			ScriptContext.GLOBAL_SCOPE);
		
		if ((functionObj == null) || !(functionObj instanceof ParseStatement))
			throw new ScriptException(String.format(
				"Unable to find function %s.", _functionName));
		
		Object []parameters = new Object[_parameters.size()];
		int lcv = 0;
		for (ParseStatement stmt : _parameters)
			parameters[lcv++] = stmt.evaluate(context);
		
		Object ret;
		
		try
		{
			context.push();		
			context.setAttribute("ARGV", parameters);
			
			ret = ((ParseStatement)functionObj).evaluate(context);
		}
		catch (ReturnFromFunctionException rffe)
		{
			ret = rffe.getResult();
		}
		finally
		{
			context.pop();
		}
		
		if (_property != null)
			context.setAttribute(MacroReplacer.replaceMacros(
				context, _property), ret);
		
		return ret;
	}
}