package edu.virginia.vcgr.genii.client.exec;

public abstract class AbstractExecutionTask implements ExecutionTask
{
	private ExecutionResultsChecker _resultsChecker;

	protected AbstractExecutionTask(ExecutionResultsChecker resultsChecker)
	{
		_resultsChecker = resultsChecker;
	}

	protected AbstractExecutionTask(int expectedExitCode)
	{
		this(new DefaultResultsChecker(expectedExitCode));
	}

	protected AbstractExecutionTask()
	{
		this(0);
	}

	@Override
	final public ExecutionResultsChecker getResultsChecker()
	{
		return _resultsChecker;
	}
}