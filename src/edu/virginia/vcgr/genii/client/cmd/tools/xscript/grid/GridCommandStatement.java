package edu.virginia.vcgr.genii.client.cmd.tools.xscript.grid;

import java.util.Collection;
import java.util.LinkedList;

import javax.script.ScriptException;

import edu.virginia.vcgr.genii.client.cmd.CommandLineRunner;
import edu.virginia.vcgr.xscript.EarlyExitException;
import edu.virginia.vcgr.xscript.ParseStatement;
import edu.virginia.vcgr.xscript.ReturnFromFunctionException;
import edu.virginia.vcgr.xscript.XScriptContext;

public class GridCommandStatement implements ParseStatement
{
	private String _commandName;
	private Collection<ParseStatement> _arguments;
	
	public GridCommandStatement(String commandName)
	{
		_commandName = commandName;
		_arguments = new LinkedList<ParseStatement>();
	}
	
	public void addArgument(ParseStatement argument)
	{
		_arguments.add(argument);
	}
	
	@Override
	public Object evaluate(XScriptContext context) throws ScriptException,
		EarlyExitException, ReturnFromFunctionException
	{
		String []cLine = new String[_arguments.size() + 1];
		cLine[0] = _commandName;
		int lcv = 1;
		for (ParseStatement stmt : _arguments)
			cLine[lcv] = stmt.evaluate(context).toString();
		
		try
		{
			return
				new CommandLineRunner().runCommand(cLine,
					context.getWriter(), context.getErrorWriter(),
					context.getReader());
		}
		catch (ScriptException se)
		{
			throw se;
		}
		catch (EarlyExitException eee)
		{
			throw eee;
		}
		catch (ReturnFromFunctionException rffe)
		{
			throw rffe;
		}
		catch (RuntimeException re)
		{
			throw re;
		}
		catch (Throwable cause)
		{
			throw new ScriptException((Exception)cause);
		}
	}
}