package edu.virginia.vcgr.genii.client.exec;

public interface ExecutionTask
{
	public String[] getCommandLine();

	public ExecutionResultsChecker getResultsChecker();
}