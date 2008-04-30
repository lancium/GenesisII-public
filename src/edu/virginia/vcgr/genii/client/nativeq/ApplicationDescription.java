package edu.virginia.vcgr.genii.client.nativeq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ApplicationDescription
{
	private String _executableName;
	private Collection<String> _arguments;
	private Map<String, String> _environment;
	private String _stdinRedirect;
	private String _stdoutRedirect;
	private String _stderrRedirect;
	
	public ApplicationDescription(
		String executableName, Collection<String> arguments,
		Map<String, String> environment,
		String stdinRedirect, String stdoutRedirect, String stderrRedirect)
	{
		if (executableName == null)
			throw new IllegalArgumentException(
				"Executable name cannot be null.");
		
		if (arguments == null)
			arguments = new ArrayList<String>();
		if (environment == null)
			environment = new HashMap<String, String>();
		
		_executableName = executableName;
		_arguments = arguments;
		_environment = environment;
		_stdinRedirect = stdinRedirect;
		_stdoutRedirect = stdoutRedirect;
		_stderrRedirect = stderrRedirect;
	}

	public String getExecutableName()
	{
		return _executableName;
	}

	public Collection<String> getArguments()
	{
		return _arguments;
	}

	public Map<String, String> getEnvironment()
	{
		return _environment;
	}

	public String getStdinRedirect()
	{
		return _stdinRedirect;
	}

	public String getStdoutRedirect()
	{
		return _stdoutRedirect;
	}

	public String getStderrRedirect()
	{
		return _stderrRedirect;
	}
}