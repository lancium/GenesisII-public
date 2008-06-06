package edu.virginia.vcgr.genii.container.bes.jsdl.personality.simpleexec;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.bes.GeniiBESConstants;
import edu.virginia.vcgr.genii.client.configuration.Deployment;
import edu.virginia.vcgr.genii.client.configuration.Installation;
import edu.virginia.vcgr.genii.client.configuration.OGRSHVersion;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.jsdl.InvalidJSDLException;
import edu.virginia.vcgr.genii.client.jsdl.JSDLException;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.tty.TTYConstants;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.ByteIORedirectionSink;
import edu.virginia.vcgr.genii.container.bes.execution.phases.FileRedirectionSink;
import edu.virginia.vcgr.genii.container.bes.execution.phases.FileRedirectionSource;
import edu.virginia.vcgr.genii.container.bes.execution.phases.PrepareApplicationPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.QueueProcessPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.RunProcessPhase;
import edu.virginia.vcgr.genii.container.bes.execution.phases.StreamRedirectionDescription;
import edu.virginia.vcgr.genii.container.bes.execution.phases.StreamRedirectionSink;
import edu.virginia.vcgr.genii.container.bes.execution.phases.StreamRedirectionSource;
import edu.virginia.vcgr.genii.container.bes.execution.phases.TeeRedirectionSink;

public class ForkExecUnderstanding implements Application
{
	static private Log _logger = LogFactory.getLog(ForkExecUnderstanding.class);
	
	private String _workingDirectory = null;
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
	
	public void setWorkingDirectory(String wd)
	{
		_workingDirectory = wd;
	}
	
	public String getWorkingDirectory()
	{
		return _workingDirectory;
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
	public void addExecutionPhases(Properties creationProperties,
		Vector<ExecutionPhase> executionPlan,
		Vector<ExecutionPhase> cleanupPhases, String ogrshVersion)
			throws JSDLException
	{
		if (creationProperties != null && creationProperties.getProperty(
			GeniiBESConstants.NATIVEQ_PROVIDER_PROPERTY) != null)
		{
			addQueuePhases(creationProperties, executionPlan,
				cleanupPhases, ogrshVersion);
		} else
		{
			addForkExecPhases(creationProperties, executionPlan,
				cleanupPhases, ogrshVersion);
		}
	}
	
	private void addQueuePhases(Properties creationProperties, 
		Vector<ExecutionPhase> executionPlan,
		Vector<ExecutionPhase> cleanupPhases, 
		String ogrshVersion) throws JSDLException
	{
		Deployment deployment = Installation.getDeployment();
		executionPlan.add(new PrepareApplicationPhase(_executable));
		
		String depName = deployment.getName();
		_logger.debug("Setting deployment name to \"" + depName + "\".");
		_environment.put("GENII_DEPLOYMENT_NAME", depName);
		
		if (ogrshVersion == null)
		{
			executionPlan.add(new QueueProcessPhase(
				_executable, _arguments, _environment,
				_stdinRedirect, _stdoutRedirect, _stderrRedirect,
				creationProperties));
		} else
		{
			_environment.put("BES_HOME", "/home/bes-job");
			_environment.put("OGRSH_CONFIG", "./ogrsh-config.xml");
			_environment.put("GENII_USER_DIR", ".");
			
			Vector<String> args = new Vector<String>();
			args.add(_executable);
			args.addAll(_arguments);
			
			OGRSHVersion oVersion = Installation.getOGRSH(
				).getInstalledVersions().get(ogrshVersion);
			File shim = oVersion.shimScript();
			_executable = shim.getAbsolutePath();
			
			executionPlan.add(new QueueProcessPhase(
				_executable, args, _environment,
				_stdinRedirect, _stdoutRedirect, _stderrRedirect,
				creationProperties));
		}
	}
	
	private void addForkExecPhases(Properties creationProperties, 
		Vector<ExecutionPhase> executionPlan,
		Vector<ExecutionPhase> cleanupPhases, 
		String ogrshVersion) throws JSDLException
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
		
		try
		{
			byte[] data = (byte[])ContextManager.getCurrentContext().getSingleValueProperty(
				TTYConstants.TTY_CALLING_CONTEXT_PROPERTY);
			if (data != null)
			{
				ByteIORedirectionSink sink = new ByteIORedirectionSink(
					EPRUtils.fromBytes(data));
				if (stdout != null)
					stdout = new TeeRedirectionSink(stdout, sink);
				else
					stdout = sink;
				
				if (stderr != null)
					stderr = new TeeRedirectionSink(stderr, sink);
				else
					stderr = sink;
			}
		}
		catch (Exception e)
		{
			_logger.warn("Error trying to get the TTY property.", e);
		}
		
		Deployment deployment = Installation.getDeployment();
		executionPlan.add(new PrepareApplicationPhase(_executable));
		
		String depName = deployment.getName();
		_logger.debug("Setting deployment name to \"" + depName + "\".");
		_environment.put("GENII_DEPLOYMENT_NAME", depName);
		
		if (ogrshVersion == null)
		{
			executionPlan.add(new RunProcessPhase(
				_executable, _arguments.toArray(new String[0]),
				_environment, 
				new StreamRedirectionDescription(stdin, stdout, stderr)));
		} else
		{
			_environment.put("BES_HOME", "/home/bes-job");
			_environment.put("OGRSH_CONFIG", "./ogrsh-config.xml");
			_environment.put("GENII_USER_DIR", ".");
			
			Vector<String> args = new Vector<String>();
			args.add(_executable);
			args.addAll(_arguments);
			
			File shim = Installation.getOGRSH(
				).getInstalledVersions().get(ogrshVersion).shimScript();
			_executable = shim.getAbsolutePath();
			
			executionPlan.add(new RunProcessPhase(
				_executable, args.toArray(new String[0]), _environment,
				new StreamRedirectionDescription(stdin, stdout, stderr)));
		}
	}
}
