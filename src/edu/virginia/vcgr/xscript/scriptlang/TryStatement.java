package edu.virginia.vcgr.xscript.scriptlang;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import javax.script.ScriptException;

import edu.virginia.vcgr.xscript.EarlyExitException;
import edu.virginia.vcgr.xscript.ParseStatement;
import edu.virginia.vcgr.xscript.ReturnFromFunctionException;
import edu.virginia.vcgr.xscript.XScriptContext;

public class TryStatement implements ParseStatement
{
	static private class CatchStructure
	{
		private Class<? extends Throwable> _catchClass;
		private ParseStatement _statement;

		@SuppressWarnings("unchecked")
		public CatchStructure(String className, ParseStatement statement)
			throws ScriptException, ClassNotFoundException
		{
			_statement = statement;
			Class<?> clazz;
			clazz = Thread.currentThread().getContextClassLoader()
				.loadClass(className);
			if (!Throwable.class.isAssignableFrom(clazz))
				throw new ScriptException(String.format(
					"Class \"%s\" is not a Throwable.", className));
			_catchClass = (Class<? extends Throwable>)clazz;
		}
	}

	private ParseStatement _tryBlock;
	private ParseStatement _finallyBlock;
	private Collection<CatchStructure> _catches;

	public TryStatement(ParseStatement tryBlock,
		Map<String, ParseStatement> catches, ParseStatement finallyBlock)
			throws ScriptException
	{
		_tryBlock = tryBlock;
		_finallyBlock = finallyBlock;
		
		_catches = new LinkedList<CatchStructure>();
		for (String className : catches.keySet())
		{
			try
			{
				_catches.add(new CatchStructure(
					className, catches.get(className)));
			}
			catch (ClassNotFoundException cnfe)
			{
				// Do nothing, let it go quietly.
			}
		}
	}

	@Override
	public Object evaluate(XScriptContext context) throws ScriptException,
			EarlyExitException, ReturnFromFunctionException
	{
		Object ret = null;
		
		if (_tryBlock != null)
		{
			try
			{
				ret = _tryBlock.evaluate(context);
			}
			catch (Throwable cause)
			{
				if (cause instanceof ScriptException)
				{
					if (((ScriptException)cause).getCause() != null)
						cause = ((ScriptException)cause).getCause();
				}
				
				boolean handled = false;
				for (CatchStructure structure : _catches)
				{
					if (structure._catchClass.isAssignableFrom(
						cause.getClass()))
					{
						handled = true;
						context.setException(cause);
						ret = structure._statement.evaluate(context);
						break;
					}
				}
				
				if (!handled)
				{
					if (cause instanceof ScriptException)
						throw (ScriptException)cause;
					else if (cause instanceof EarlyExitException)
						throw (EarlyExitException)cause;
					else if (cause instanceof ReturnFromFunctionException)
						throw (ReturnFromFunctionException)cause;
					else if (cause instanceof RuntimeException)
						throw (RuntimeException)cause;
					else
						throw new ScriptException((Exception)cause);
				}
			}
			finally
			{
				if (_finallyBlock != null)
					_finallyBlock.evaluate(context);
			}
		}
		
		return ret;
	}
}