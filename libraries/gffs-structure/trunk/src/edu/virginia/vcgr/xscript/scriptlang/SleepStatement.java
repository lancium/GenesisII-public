package edu.virginia.vcgr.xscript.scriptlang;

import java.util.concurrent.TimeUnit;

import javax.script.ScriptException;

import edu.virginia.vcgr.xscript.EarlyExitException;
import edu.virginia.vcgr.xscript.ParseStatement;
import edu.virginia.vcgr.xscript.ReturnFromFunctionException;
import edu.virginia.vcgr.xscript.XScriptContext;

public class SleepStatement implements ParseStatement
{
	private TimeUnit _unit;
	private long _timeout;

	public SleepStatement(TimeUnit unit, long timeout)
	{
		_unit = unit;
		_timeout = timeout;
	}

	@Override
	public Object evaluate(XScriptContext context) throws ScriptException, EarlyExitException, ReturnFromFunctionException
	{
		try {
			_unit.sleep(_timeout);
		} catch (InterruptedException ie) {
		}
		return Boolean.TRUE;
	}
}