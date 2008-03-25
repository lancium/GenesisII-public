package edu.virginia.vcgr.genii.container.bes.jsdl.personality.simpleexec;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;

import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.jsdl.InvalidJSDLException;
import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.FileRedirectionSink;
import edu.virginia.vcgr.genii.container.bes.execution.phases.FileRedirectionSource;
import edu.virginia.vcgr.genii.container.bes.execution.phases.PrepareApplicationPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.RunProcessPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.StreamRedirectionDescription;
import edu.virginia.vcgr.genii.container.bes.execution.phases.StreamRedirectionSink;
import edu.virginia.vcgr.genii.container.bes.execution.phases.StreamRedirectionSource;

public class ForkExecUnderstanding implements Application
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
	
	public void addArgument(String arg)
	{
		_arguments.add(arg);
	}
	
	public void setStdinRedirect(String stdinRedirect)
	{
		_stderrRedirect = stdinRedirect;
	}
	
	public void setStdoutRedirect(String stdoutRedirect)
	{
		_stdoutRedirect = stdoutRedirect;
	}
	
	public void setStderrRedirect(String stderrRedirect)
	{
		_stderrRedirect = stderrRedirect;
	}
	
	public void addEnvironment(String variable, String value)
	{
		_environment.put(variable, value);
	}
	
	public void validate() throws JSDLException
	{
		if (_executable == null)
			throw new InvalidJSDLException(
				"Can't run JSDL without an executable to run.");
	}

	@Override
	public void addExecutionPhases(Vector<ExecutionPhase> executionPlan,
		Vector<ExecutionPhase> cleanupPhases, String ogrshVersion)
			throws JSDLException
	{
		StreamRedirectionSource stdin = null;
		StreamRedirectionSink stdout = null;
		StreamRedirectionSink stderr = null;
		
		if (_stdinRedirect != null)
			stdin = new FileRedirectionSource(_stdinRedirect);
		if (_stdoutRedirect != null)
			stdout = new FileRedirectionSink(_stdoutRedirect);
		if (_stderrRedirect != null)
			stderr = new FileRedirectionSink(_stderrRedirect);
		
		executionPlan.add(new PrepareApplicationPhase(_executable));
		
		if (ogrshVersion == null)
		{
			executionPlan.add(new RunProcessPhase(
				_executable, _arguments.toArray(new String[0]),
				_environment, 
				new StreamRedirectionDescription(stdin, stdout, stderr)));
		} else
		{
			_environment.put("BES_HOME", "/home/bes-job");
			_environment.put("OGRSH_CONFIG", "ogrsh-config.xml");
			
			Vector<String> args = new Vector<String>();
			args.add(_executable);
			args.addAll(_arguments);
			
			File shim = new File(ConfigurationManager.getInstallDir(), 
				"OGRSH");
			shim = new File(shim, "shim-" + ogrshVersion + ".sh");
			_executable = shim.getAbsolutePath();
			
			executionPlan.add(new RunProcessPhase(
				_executable, args.toArray(new String[0]), _environment,
				new StreamRedirectionDescription(stdin, stdout, stderr)));
		}
	}
}