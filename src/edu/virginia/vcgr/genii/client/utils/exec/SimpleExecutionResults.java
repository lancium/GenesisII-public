package edu.virginia.vcgr.genii.client.utils.exec;

import java.util.List;

public class SimpleExecutionResults
{
	private int _exitCode;
	private List<String> _output;
	private List<String> _error;
	
	public SimpleExecutionResults(int exitCode, 
		List<String> output, List<String> error)
	{
		_exitCode = exitCode;
		_output = output;
		_error = error;
	}
	
	public int getExitCode()
	{
		return _exitCode;
	}
	
	public List<String> getOutput()
	{
		return _output;
	}
	
	public List<String> getError()
	{
		return _error;
	}
}