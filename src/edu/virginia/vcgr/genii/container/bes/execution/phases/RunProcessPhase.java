package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.factory.ActivityStateEnumeration;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.bes.BESConstructionParameters;
import edu.virginia.vcgr.genii.client.pwrapper.ExitResults;
import edu.virginia.vcgr.genii.client.pwrapper.ProcessWrapper;
import edu.virginia.vcgr.genii.client.pwrapper.ProcessWrapperFactory;
import edu.virginia.vcgr.genii.client.pwrapper.ProcessWrapperToken;
import edu.virginia.vcgr.genii.container.bes.execution.ContinuableExecutionException;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionException;
import edu.virginia.vcgr.genii.container.bes.execution.TerminateableExecutionPhase;
import edu.virginia.vcgr.genii.container.cservices.ContainerServices;
import edu.virginia.vcgr.genii.container.cservices.accounting.AccountingService;

public class RunProcessPhase extends AbstractRunProcessPhase 
	implements TerminateableExecutionPhase, Serializable
{
	static final long serialVersionUID = 0L;
	
	static final private String EXECUTING_STAGE = "executing";
	
	static private Log _logger = LogFactory.getLog(RunProcessPhase.class);
	
	private File _commonDirectory;
	private File _executable;
	private String []_arguments;
	private Map<String, String> _environment;
	transient private ProcessWrapperToken _process = null;
	private String _processLock = new String();
	transient private Boolean _hardTerminate = null;
	transient private boolean _countAsFailedAttempt = true;
	
	private PassiveStreamRedirectionDescription _redirects;
	
	static private void destroyProcess(ProcessWrapperToken process)
	{
		process.cancel();
	}
	
	public RunProcessPhase(File commonDirectory,
		File executable, String []arguments, 
		Map<String, String> environment,
		PassiveStreamRedirectionDescription redirects,
		BESConstructionParameters constructionParameters)
	{
		super(new ActivityState(ActivityStateEnumeration.Running,
			EXECUTING_STAGE, false), constructionParameters);
		
		_commonDirectory = commonDirectory;
		
		if (executable == null)
			throw new IllegalArgumentException(
				"Argument \"executable\" cannot be null.");
		if (arguments == null)
			arguments = new String[0];
		
		if (redirects == null)
			redirects = new PassiveStreamRedirectionDescription(
				null, null, null);
		
		_executable = executable;
		_arguments = arguments;
		_environment = environment;
		
		_redirects = redirects;
	}

	protected void finalize() throws Throwable
	{
		synchronized(_processLock)
		{
			if (_process != null)
				destroyProcess(_process);
		}
	}
	
	@Override
	public void execute(ExecutionContext context) throws Throwable
	{
		List<String> command;
		ProcessWrapperToken token;
		synchronized(_processLock)
		{
			command = new Vector<String>();
			command.add(_executable.getAbsolutePath());
			for (String arg : _arguments)
				command.add(arg);
			
			File workingDirectory = context.getCurrentWorkingDirectory(
				).getWorkingDirectory();
			
			ProcessWrapper wrapper = ProcessWrapperFactory.createWrapper(
				_commonDirectory);
		
			if (_environment == null)
				_environment = new HashMap<String, String>();
			
			setExportedEnvironment(_environment);
			
			if (_environment != null)
			{
				String ogrshConfig = _environment.get("OGRSH_CONFIG");
				if (ogrshConfig != null)
				{
					File f = new File(
						workingDirectory, ogrshConfig);
					_environment.put("OGRSH_CONFIG", f.getAbsolutePath());
				}
				
				String geniiUserDir = _environment.get("GENII_USER_DIR");
				if (geniiUserDir != null && !geniiUserDir.startsWith("/"))
				{
					File f = new File(
						workingDirectory, geniiUserDir);
					_environment.put("GENII_USER_DIR", f.getAbsolutePath());
				}
				
				_environment = overloadEnvironment(_environment);
			}
			resetCommand(command, workingDirectory, _environment);
			
			_logger.info("Trying to start a new process on machine using fork/exec or spawn.");
			String []arguments = new String[command.size() - 1];
			for (int lcv = 1; lcv < command.size(); lcv++)
				arguments[lcv - 1] = command.get(lcv);
			preDelay();
			token = wrapper.execute(_environment, workingDirectory, 
				_redirects.stdinSource(), _redirects.stdoutSink(), 
				_redirects.stderrSink(), command.get(0), arguments);
		}
		
		try
		{
			token.join();
			postDelay();
			ExitResults results = token.results();
			
			if (_hardTerminate != null && _hardTerminate.booleanValue())
			{
				if (_countAsFailedAttempt)
					throw new ContinuableExecutionException(
						"The process was forcably killed.");
				
				return;
			}
			
			if (results == null)
				_logger.error("Somehow we got an exit with no exit results.");
			else
			{
				_logger.info(String.format("Process exited with exit-code %d.",
					results.exitCode()));
				
				AccountingService acctService = 
					ContainerServices.findService(AccountingService.class);
				if (acctService != null)
				{
					acctService.addAccountingRecord(
						context.getCallingContext(), context.getBESEPI(),
						null, null, null, command,
						results.exitCode(),
						results.userTime(), results.kernelTime(),
						results.wallclockTime(), results.maximumRSS());
				}
			}
		}
		catch (InterruptedException ie)
		{
			synchronized(_processLock)
			{
				destroyProcess(_process);
				_process = null;
			}
		}
		finally
		{
			synchronized(_processLock)
			{
				_process = null;
			}
		}
	}
	
	@Override
	public void terminate(boolean countAsFailedAttempt) 
		throws ExecutionException
	{
		synchronized(_processLock)
		{
			_hardTerminate = Boolean.TRUE;
			_countAsFailedAttempt = countAsFailedAttempt;
			destroyProcess(_process);
		}
	}
}
