package edu.virginia.vcgr.genii.container.bes.jsdl.personality.common;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import edu.virginia.vcgr.genii.client.jsdl.InvalidJSDLException;
import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.container.bes.execution.phases.FileRedirectionSink;
import edu.virginia.vcgr.genii.container.bes.execution.phases.FileRedirectionSource;
import edu.virginia.vcgr.genii.container.bes.execution.phases.StreamRedirectionDescription;
import edu.virginia.vcgr.genii.container.bes.execution.phases.StreamRedirectionSink;
import edu.virginia.vcgr.genii.container.bes.execution.phases.StreamRedirectionSource;
import edu.virginia.vcgr.genii.container.bes.execution.phases.TeeRedirectionSink;

public abstract class PosixLikeApplicationUnderstanding extends
		CommonApplicationUnderstanding
{
	private String _executable = null;
	private Collection<String> _arguments =
		new LinkedList<String>();
	private String _stdinRedirect = null;
	private String _stdoutRedirect = null;
	private String _stderrRedirect = null;
	private Map<String, String> _environment =
		new HashMap<String, String>();
	
	public void setExecutable(String executable)
	{
		_executable = executable;
	}
	
	public String getExecutable()
	{
		return _executable;
	}
	
	public void addArgument(String arg)
	{
		_arguments.add(arg);
	}
	
	public Collection<String> getArguments()
	{
		return _arguments;
	}
	
	public void setStdinRedirect(String stdinRedirect)
	{
		_stdinRedirect = stdinRedirect;
	}
	
	public String getStdinRedirect()
	{
		return _stdinRedirect;
	}
	
	public void setStdoutRedirect(String stdoutRedirect)
	{
		_stdoutRedirect = stdoutRedirect;
	}
	
	public String getStdoutRedirect()
	{
		return _stdoutRedirect;
	}
	
	public void setStderrRedirect(String stderrRedirect)
	{
		_stderrRedirect = stderrRedirect;
	}
	
	public String getStderrRedirect()
	{
		return _stderrRedirect;
	}
	
	public void addEnvironment(String variable, String value)
	{
		_environment.put(variable, value);
	}
	
	public Map<String, String> getEnvironment()
	{
		return _environment;
	}
	
	public void validate() throws JSDLException
	{
		if (_executable == null)
			throw new InvalidJSDLException(
				"Can't run JSDL without an executable to run.");
	}
	
	protected StreamRedirectionDescription getStreamRedirectionDescription()
	{
		StreamRedirectionSource stdin = null;
		StreamRedirectionSink stdout = null;
		StreamRedirectionSink stderr = null;
		
		String stdinRedirect = getStdinRedirect();
		String stdoutRedirect = getStdoutRedirect();
		String stderrRedirect = getStderrRedirect();
		
		if (stdinRedirect != null)
			stdin = new FileRedirectionSource(stdinRedirect);
		if (stdoutRedirect != null)
			stdout = new FileRedirectionSink(stdoutRedirect);
		if (stderrRedirect != null)
			stderr = new FileRedirectionSink(stderrRedirect);
		
		StreamRedirectionSink tty = discoverTTYRedirectionSink();
		if (tty != null)
		{
			if (stdout != null)
				stdout = new TeeRedirectionSink(stdout, tty);
			else
				stdout = tty;
			
			if (stderr != null)
				stderr = new TeeRedirectionSink(stderr, tty);
			else
				stderr = tty;
		}
		
		return new StreamRedirectionDescription(stdin, stdout, stderr);
	}
}