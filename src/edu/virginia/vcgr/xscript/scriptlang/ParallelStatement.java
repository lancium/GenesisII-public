package edu.virginia.vcgr.xscript.scriptlang;

import javax.script.ScriptException;

import edu.virginia.vcgr.xscript.EarlyExitException;
import edu.virginia.vcgr.xscript.ParseStatement;
import edu.virginia.vcgr.xscript.ReturnFromFunctionException;
import edu.virginia.vcgr.xscript.XScriptContext;
import edu.virginia.vcgr.xscript.macros.MacroReplacer;

class ParallelStatement implements ParseStatement
{
	private String _threadPoolSizeString;
	private ParseStatement _innerStatement;
	
	ParallelStatement(String threadPoolSizeString, 
		ParseStatement innerStatement)
	{
		_threadPoolSizeString = threadPoolSizeString;
		_innerStatement = innerStatement;
	}
	
	@Override
	public Object evaluate(XScriptContext context) throws ScriptException,
			EarlyExitException, ReturnFromFunctionException
	{
		int threadPoolSize = Integer.parseInt(MacroReplacer.replaceMacros(
			context, _threadPoolSizeString));
		ParallelJobPool pool = new ParallelJobPool(threadPoolSize);
		try
		{
			context.setAttribute(
				ParallelJobPool.PARALLEL_JOB_POOL_BINDING_NAME, pool);
			return _innerStatement.evaluate(context);
		}
		finally
		{
			pool.blockAndStop();
		}
	}
}