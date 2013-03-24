package edu.virginia.vcgr.genii.container.bes.jsdl.personality.common;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import edu.virginia.vcgr.genii.client.jsdl.FilesystemManager;
import edu.virginia.vcgr.genii.client.jsdl.FilesystemRelativePath;
import edu.virginia.vcgr.genii.client.jsdl.InvalidJSDLException;
import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.container.bes.execution.phases.PassiveStreamRedirectionDescription;

public abstract class PosixLikeApplicationUnderstanding extends CommonApplicationUnderstanding
{
	private Integer _numProcesses = null;
	private Integer _numProcessesPerHost = null;
	private URI _spmdVariation = null;

	private FilesystemRelativePath _executable = null;
	private Collection<StringOrPath> _arguments = new LinkedList<StringOrPath>();
	private FilesystemRelativePath _stdinRedirect = null;
	private FilesystemRelativePath _stdoutRedirect = null;
	private FilesystemRelativePath _stderrRedirect = null;
	private Map<String, StringOrPath> _environment = new HashMap<String, StringOrPath>();

	protected PosixLikeApplicationUnderstanding(FilesystemManager fsManager, BESWorkingDirectory workingDirectory)
	{
		super(fsManager, workingDirectory);
	}

	public void setSPMDVariation(URI spmdVariation)
	{
		_spmdVariation = spmdVariation;
	}

	public URI getSPMDVariation()
	{
		return _spmdVariation;
	}

	public void setNumProcesses(int numProcesses)
	{
		_numProcesses = new Integer(numProcesses);
	}

	public Integer getNumProcesses()
	{
		return _numProcesses;
	}

	public void setNumProcessesPerHost(int numProcessesPerHost)
	{
		_numProcessesPerHost = new Integer(numProcessesPerHost);
	}

	public Integer getNumProcessesPerHost()
	{
		return _numProcessesPerHost;
	}

	public void setExecutable(FilesystemRelativePath executable)
	{
		_executable = executable;
	}

	public FilesystemRelativePath getExecutable()
	{
		return _executable;
	}

	public void addArgument(StringOrPath arg)
	{
		_arguments.add(arg);
	}

	public Collection<StringOrPath> getArguments()
	{
		return _arguments;
	}

	public void setStdinRedirect(FilesystemRelativePath stdinRedirect)
	{
		_stdinRedirect = stdinRedirect;
	}

	public FilesystemRelativePath getStdinRedirect()
	{
		return _stdinRedirect;
	}

	public void setStdoutRedirect(FilesystemRelativePath stdoutRedirect)
	{
		_stdoutRedirect = stdoutRedirect;
	}

	public FilesystemRelativePath getStdoutRedirect()
	{
		return _stdoutRedirect;
	}

	public void setStderrRedirect(FilesystemRelativePath stderrRedirect)
	{
		_stderrRedirect = stderrRedirect;
	}

	public FilesystemRelativePath getStderrRedirect()
	{
		return _stderrRedirect;
	}

	public void addEnvironment(String variable, StringOrPath value)
	{
		_environment.put(variable, value);
	}

	public Map<String, StringOrPath> getEnvironment()
	{
		return _environment;
	}

	public void validate() throws JSDLException
	{
		if (_executable == null)
			throw new InvalidJSDLException("Can't run JSDL without an executable to run.");
	}

	protected PassiveStreamRedirectionDescription getStreamRedirectionDescription() throws JSDLException
	{
		/*
		 * Old way StreamRedirectionSource stdin = null; StreamRedirectionSink stdout = null;
		 * StreamRedirectionSink stderr = null;
		 * 
		 * FilesystemRelativePath stdinRedirect = getStdinRedirect(); FilesystemRelativePath
		 * stdoutRedirect = getStdoutRedirect(); FilesystemRelativePath stderrRedirect =
		 * getStderrRedirect();
		 * 
		 * if (stdinRedirect != null) stdin = new FileRedirectionSource(
		 * getFilesystemManager().lookup(stdinRedirect)); if (stdoutRedirect != null) stdout = new
		 * FileRedirectionSink( getFilesystemManager().lookup(stdoutRedirect)); if (stderrRedirect
		 * != null) stderr = new FileRedirectionSink(
		 * getFilesystemManager().lookup(stderrRedirect));
		 * 
		 * StreamRedirectionSink tty = discoverTTYRedirectionSink(); if (tty != null) { if (stdout
		 * != null) stdout = new TeeRedirectionSink(stdout, tty); else stdout = tty;
		 * 
		 * if (stderr != null) stderr = new TeeRedirectionSink(stderr, tty); else stderr = tty; }
		 */

		File stdin = null;
		File stdout = null;
		File stderr = null;

		FilesystemRelativePath stdinRedirect = getStdinRedirect();
		FilesystemRelativePath stdoutRedirect = getStdoutRedirect();
		FilesystemRelativePath stderrRedirect = getStderrRedirect();

		if (stdinRedirect != null)
			stdin = getFilesystemManager().lookup(stdinRedirect);
		if (stdoutRedirect != null)
			stdout = getFilesystemManager().lookup(stdoutRedirect);
		if (stderrRedirect != null)
			stderr = getFilesystemManager().lookup(stderrRedirect);

		return new PassiveStreamRedirectionDescription(stdin, stdout, stderr);
	}
}