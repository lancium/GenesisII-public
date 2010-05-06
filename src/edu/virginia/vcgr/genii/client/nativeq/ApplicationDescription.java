package edu.virginia.vcgr.genii.client.nativeq;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import edu.virginia.vcgr.genii.container.bes.jsdl.personality.common.ResourceConstraints;

public class ApplicationDescription
{
	private URI _spmdVariation;
	private Integer _numProcesses;
	private Integer _numProcessesPerHost;
	private String _executableName;
	private Collection<String> _arguments;
	private Map<String, String> _environment;
	private String _stdinRedirect;
	private String _stdoutRedirect;
	private String _stderrRedirect;
	private ResourceConstraints _resourceConstraints;
	private File _resourceUsagePath;
	
	public ApplicationDescription(
		URI spmdVariation, Integer numProcesses, Integer numProcessesPerHost,
		String executableName, Collection<String> arguments,
		Map<String, String> environment,
		String stdinRedirect, String stdoutRedirect, String stderrRedirect,
		ResourceConstraints resourceConstraints, File resourceUsagePath)
	{
		_spmdVariation = spmdVariation;
		_numProcesses = numProcesses;
		_numProcessesPerHost = numProcessesPerHost;
		
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
		
		_resourceConstraints = resourceConstraints;
		_resourceUsagePath = resourceUsagePath;
	}

	public URI getSPMDVariation()
	{
		return _spmdVariation;
	}
	
	public Integer getNumProcesses()
	{
		return _numProcesses;
	}
	
	public Integer getNumProcessesPerHost()
	{
		return _numProcessesPerHost;
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

	public File getStdinRedirect(File workingDirectory)
	{
		if (_stdinRedirect == null)
			return null;
		
		File stdin = new File(_stderrRedirect);
		if (stdin.isAbsolute())
			return stdin;
		
		return new File(workingDirectory, _stdinRedirect);
	}

	public File getStdoutRedirect(File workingDirectory)
	{
		if (_stdoutRedirect == null)
			return null;
		
		File stdout = new File(_stdoutRedirect);
		if (stdout.isAbsolute())
			return stdout;
		
		return new File(workingDirectory, _stdoutRedirect);
	}

	public File getStderrRedirect(File workingDirectory)
	{
		if (_stderrRedirect == null)
			return null;
		
		File stderr = new File(_stderrRedirect);
		if (stderr.isAbsolute())
			return stderr;
		
		return new File(workingDirectory, _stderrRedirect);
	}
	
	public ResourceConstraints getResourceConstraints()
	{
		return _resourceConstraints;
	}
	
	public File getResourceUsagePath()
	{
		return _resourceUsagePath;
	}
}