package edu.virginia.vcgr.genii.client.exec;

public interface ExecutionResultsChecker
{
	public void checkResults(int exitCode) throws ExecutionException;
}