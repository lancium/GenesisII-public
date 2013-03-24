package edu.virginia.vcgr.xscript.scriptlang;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.script.ScriptException;

import edu.virginia.vcgr.xscript.EarlyExitException;
import edu.virginia.vcgr.xscript.ParseStatement;
import edu.virginia.vcgr.xscript.ReturnFromFunctionException;
import edu.virginia.vcgr.xscript.XScriptContext;
import edu.virginia.vcgr.xscript.macros.MacroReplacer;

public class ThrowStatement implements ParseStatement
{
	private Constructor<? extends Throwable> _constructor;
	private String _message;

	@SuppressWarnings("unchecked")
	public ThrowStatement(String className, String message) throws ScriptException
	{
		try {
			Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
			if (!Throwable.class.isAssignableFrom(clazz))
				throw new ScriptException(String.format("Class %s is not a Throwable.", className));
			_constructor = (Constructor<? extends Throwable>) clazz.getConstructor(String.class);
		} catch (Exception e) {
			throw new ScriptException(e);
		}

		_message = message;
	}

	@Override
	public Object evaluate(XScriptContext context) throws ScriptException, EarlyExitException, ReturnFromFunctionException
	{
		Throwable cause;

		try {
			cause = _constructor.newInstance(MacroReplacer.replaceMacros(context, _message));
		} catch (InstantiationException ie) {
			throw new ScriptException(ie);
		} catch (IllegalAccessException iae) {
			throw new ScriptException(iae);
		} catch (InvocationTargetException ite) {
			throw new ScriptException(ite);
		}

		if (cause instanceof ScriptException)
			throw (ScriptException) cause;
		else if (cause instanceof EarlyExitException)
			throw (EarlyExitException) cause;
		else if (cause instanceof ReturnFromFunctionException)
			throw (ReturnFromFunctionException) cause;
		else if (cause instanceof RuntimeException)
			throw (RuntimeException) cause;

		throw new ScriptException((Exception) cause);
	}
}