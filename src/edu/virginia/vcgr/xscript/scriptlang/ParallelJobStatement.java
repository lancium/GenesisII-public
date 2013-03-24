package edu.virginia.vcgr.xscript.scriptlang;

import javax.script.ScriptException;

import edu.virginia.vcgr.xscript.EarlyExitException;
import edu.virginia.vcgr.xscript.ParseStatement;
import edu.virginia.vcgr.xscript.ReturnFromFunctionException;
import edu.virginia.vcgr.xscript.XScriptContext;

public class ParallelJobStatement implements ParseStatement
{
	private ParseStatement _innerStatement;

	public ParallelJobStatement(ParseStatement innerStatement)
	{
		_innerStatement = innerStatement;
	}

	@Override
	public Object evaluate(XScriptContext context) throws ScriptException, EarlyExitException, ReturnFromFunctionException
	{
		ParallelJobPool pool = (ParallelJobPool) context.getAttribute(ParallelJobPool.PARALLEL_JOB_POOL_BINDING_NAME);
		if (pool == null)
			throw new ScriptException("Couldn't find a parallel pool in which to run " + "the parallel job.");
		pool.addParallelJob(_innerStatement, context);
		return 0;
	}
}