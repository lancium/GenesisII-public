package edu.virginia.vcgr.genii.client.nativeq;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import edu.virginia.vcgr.genii.client.jsdl.personality.common.ResourceConstraints;

public class ApplicationDescription
{
	private File _fuseMountPoint;
	private URI _spmdVariation;
	private Integer _numProcesses;
	private Integer _numProcessesPerHost;
	private Integer _threadsPerProcess;
	private String _executableName;
	private Collection<String> _arguments;
	private Map<String, String> _environment;
	private String _stdinRedirect;
	private String _stdoutRedirect;
	private String _stderrRedirect;
	private ResourceConstraints _resourceConstraints;
	private File _resourceUsagePath;
	private boolean _isRestartFromPersist;

	public ApplicationDescription(File fuseMountPoint, URI spmdVariation, Integer numProcesses, Integer numProcessesPerHost,
		Integer threadsPerProcess, String executableName, Collection<String> arguments, Map<String, String> environment, String stdinRedirect,
		String stdoutRedirect, String stderrRedirect, ResourceConstraints resourceConstraints, File resourceUsagePath, boolean isRestartFromPersist)
	{
		_fuseMountPoint = fuseMountPoint;
		_spmdVariation = spmdVariation;
		_numProcesses = numProcesses;
		_numProcessesPerHost = numProcessesPerHost;
		_threadsPerProcess = threadsPerProcess;

		if (executableName == null)
			throw new IllegalArgumentException("Executable name cannot be null.");

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
		
		//LAK 2021 Jan 29
		_isRestartFromPersist = isRestartFromPersist;
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

	public Integer getThreadsPerProcess()
	{
		return _threadsPerProcess;
	}

	public String getExecutableName()
	{
		return _executableName;
	}

	public File getFuseMountPoint()
	{
		return _fuseMountPoint;
	}

	public Collection<String> getArguments()
	{
		return _arguments;
	}

	public void setArguments(Collection<String> arguments)
	{
		_arguments = arguments;
	}

	public Map<String, String> getEnvironment()
	{
		return _environment;
	}

	public File getStdinRedirect(File workingDirectory)
	{
		if (_stdinRedirect == null)
			return null;

		File stdin = new File(_stdinRedirect);
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

	public void setStdoutRedirect(String stdoutRedirect)
	{
		_stdoutRedirect = stdoutRedirect;
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

	public void setStderrRedirect(String stderrRedirect)
	{
		_stderrRedirect = stderrRedirect;
	}

	public ResourceConstraints getResourceConstraints()
	{
		return _resourceConstraints;
	}

	public File getResourceUsagePath()
	{
		return _resourceUsagePath;
	}
	
	public boolean getIsRestartFromPersist()
	{
		return _isRestartFromPersist;
	}
}
