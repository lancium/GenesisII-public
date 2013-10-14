package edu.virginia.vcgr.xscript.scriptlang;

import java.util.LinkedList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.xscript.ParseStatement;
import edu.virginia.vcgr.xscript.XScriptContext;

public class ParallelJobPool
{
	static private Log _logger = LogFactory.getLog(ParallelJobPool.class);

	static public final String PARALLEL_JOB_POOL_BINDING_NAME = "Parallel Job Pool Binding";

	static private class ParallelJobContext
	{
		private XScriptContext _scriptContext;
		private ParseStatement _statement;

		private ParallelJobContext(ParseStatement statement, XScriptContext scriptContext)
		{
			_scriptContext = scriptContext;
			_statement = statement;
		}

		private void run()
		{
			try {
				_statement.evaluate(_scriptContext);
			} catch (Throwable cause) {
				_logger.warn("Parallel script job failed.", cause);
			}
		}
	}

	private boolean _finished = false;
	private LinkedList<ParallelJobContext> _jobs = new LinkedList<ParallelJobContext>();
	private Thread[] _threads;

	public ParallelJobPool(int poolSize)
	{
		_threads = new Thread[poolSize];

		while (poolSize > 0) {
			_threads[--poolSize] = new Thread(new ParallelJobWorker(), "Script Worker");
			_threads[poolSize].setDaemon(true);
			_threads[poolSize].start();
		}
	}

	public void blockAndStop()
	{
		synchronized (_jobs) {
			_finished = true;
			_jobs.notifyAll();
		}

		for (Thread th : _threads) {
			try {
				th.join();
			} catch (InterruptedException ie) {
			}
		}
	}

	public void addParallelJob(ParseStatement statement, XScriptContext parentContext)
	{
		XScriptContext childContext;
		try {
			childContext = (XScriptContext) parentContext.clone();
		} catch (CloneNotSupportedException e) {
			_logger.error("This should not have happened.", e);
			throw new RuntimeException("Couldn't add parallel job.", e);
		}

		synchronized (_jobs) {
			_jobs.add(new ParallelJobContext(statement, childContext));
			_jobs.notify();
		}
	}

	private class ParallelJobWorker implements Runnable
	{
		@Override
		public void run()
		{
			while (true) {
				ParallelJobContext nextJob = null;
				synchronized (_jobs) {
					while (_jobs.isEmpty() && !_finished) {
						try {
							_jobs.wait();
						} catch (InterruptedException ie) {
						}
					}

					if (_jobs.isEmpty() && _finished)
						return;

					nextJob = _jobs.removeFirst();
				}

				nextJob.run();
			}
		}
	}
}