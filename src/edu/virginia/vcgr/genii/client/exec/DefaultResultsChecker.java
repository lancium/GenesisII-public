package edu.virginia.vcgr.genii.client.exec;

public class DefaultResultsChecker implements ExecutionResultsChecker
{
	private int _expectedResult;
	
	public DefaultResultsChecker(int expectedResult)
	{
		_expectedResult = expectedResult;
	}
	
	@Override
	public void checkResults(int exitCode) throws ExecutionException
	{
		if (exitCode != _expectedResult)
			throw new ExecutionException(String.format(
				"Program exited with incorrect exit code.  " +
				"Expected %d, but got %d.", _expectedResult, exitCode));
	}
}