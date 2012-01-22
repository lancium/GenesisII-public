package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.File;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.factory.ActivityStateEnumeration;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.bes.BESConstructionParameters;
import edu.virginia.vcgr.genii.client.history.HistoryEventCategory;
import edu.virginia.vcgr.genii.client.cmdLineManipulator.CmdLineManipulatorUtils;
import edu.virginia.vcgr.genii.client.pwrapper.ExitResults;
import edu.virginia.vcgr.genii.client.pwrapper.ProcessWrapper;
import edu.virginia.vcgr.genii.client.pwrapper.ProcessWrapperFactory;
import edu.virginia.vcgr.genii.client.pwrapper.ProcessWrapperToken;
import edu.virginia.vcgr.genii.client.pwrapper.ResourceUsageDirectory;
import edu.virginia.vcgr.genii.container.bes.execution.ContinuableExecutionException;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionException;
import edu.virginia.vcgr.genii.container.bes.execution.TerminateableExecutionPhase;
import edu.virginia.vcgr.genii.container.cservices.ContainerServices;
import edu.virginia.vcgr.genii.container.cservices.accounting.AccountingService;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContext;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContextFactory;

public class RunProcessPhase extends AbstractRunProcessPhase 
	implements TerminateableExecutionPhase, Serializable
{
	static final long serialVersionUID = 0L;
	
	static final private String EXECUTING_STAGE = "executing";
	
	static private Log _logger = LogFactory.getLog(RunProcessPhase.class);
	
	private URI _spmdVariation;
	private Integer _numProcesses;
	private Integer _numProcessesPerHost;
	private File _fuseMountPoint;
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
	
	public RunProcessPhase(File fuseMountPoint, URI spmdVariation, 
		Integer numProcesses, Integer numProcessesPerHost, 
		File commonDirectory, File executable, 
		String []arguments, Map<String, String> environment,
		PassiveStreamRedirectionDescription redirects,
		BESConstructionParameters constructionParameters)
	{
		super(new ActivityState(ActivityStateEnumeration.Running,
			EXECUTING_STAGE, false), constructionParameters);
		
		_spmdVariation = spmdVariation;
		_numProcesses = numProcesses;
		_numProcessesPerHost = numProcessesPerHost;
		_fuseMountPoint = fuseMountPoint;
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
		File stderrFile = null;
		List<String> command, newCmdLine;
		ProcessWrapperToken token;
		HistoryContext history = HistoryContextFactory.createContext(
			HistoryEventCategory.CreatingActivity);
		
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
			history.createDebugWriter("Activity Environment Set").format(
				"Activity environment set to %s", _environment).close();
			
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

			stderrFile = _redirects.stderrSink(workingDirectory);
						
			//assemble job properties for cmdline manipulators
			Collection<String> args = new ArrayList<String>(_arguments.length);
			for (String s : _arguments)
				args.add(s);
						
			File resourceUsageFile = new ResourceUsageDirectory(
					workingDirectory).getNewResourceUsageFile();
			
			HashMap<String, Object> jobProperties = new HashMap<String, Object>();
			CmdLineManipulatorUtils.addBasicJobProperties(jobProperties, 
					_executable.getAbsolutePath(), args);
			CmdLineManipulatorUtils.addEnvProperties(jobProperties, _fuseMountPoint,
					_environment, 
					workingDirectory, _redirects.stdinSource(), 
					_redirects.stdoutSink(), stderrFile, 
					resourceUsageFile, wrapper.getPathToWrapper());
			CmdLineManipulatorUtils.addSPMDJobProperties(jobProperties, 
					_spmdVariation, _numProcesses, _numProcessesPerHost);	
				
			newCmdLine = new Vector<String>();
			_logger.debug("Trying to call cmdLine manipulators.");
			newCmdLine = CmdLineManipulatorUtils.callCmdLineManipulators(jobProperties, 
					_constructionParameters.getCmdLineManipulatorConfiguration());
			
				//for testing only - use default cmdLine format to compare to transform
				String []arguments = new String[command.size() - 1];
				for (int lcv = 1; lcv < command.size(); lcv++)
					arguments[lcv - 1] = command.get(lcv);
				
				Vector<String> testCmdLine = wrapper.formCommandLine(_fuseMountPoint,
						_environment, workingDirectory, _redirects.stdinSource(), 
						_redirects.stdoutSink(), stderrFile, 
						resourceUsageFile, command.get(0), arguments);
				_logger.debug(String.format("Previous cmdLine format with pwrapper only:\n %s", 
						testCmdLine.toString()));
				
			_logger.debug(
				"Trying to start a new process on machine using fork/exec or spawn.");
			preDelay();
			
			
			
			PrintWriter hWriter = history.createInfoWriter(
				"BES Starting Activity").format(
					"BES starting activity: ");
			for (String arg : newCmdLine)
				hWriter.format(" %s", arg);
			hWriter.close();
			
			token = wrapper.execute(_fuseMountPoint,
				_environment, workingDirectory, 
				_redirects.stdinSource(), resourceUsageFile, newCmdLine);
		}
		
		try
		{
			token.join();
			postDelay();
			ExitResults results = token.results();
			
			if (_hardTerminate != null && _hardTerminate.booleanValue())
			{
				if (_countAsFailedAttempt)
				{
					history.warn("Process Forcably Killed");
					throw new ContinuableExecutionException(
						"The process was forcably killed.");
				}
				
				return;
			}
			
			if (results == null)
			{
				_logger.error("Somehow we got an exit with no exit results.");
				history.debug("Job Exited with No Resulst");
			} else
			{
				history.trace("Job Exited with Exit Code %d", results.exitCode());
				
				_logger.info(String.format("Process exited with exit-code %d.",
					results.exitCode()));
				
				AccountingService acctService = 
					ContainerServices.findService(AccountingService.class);
				if (acctService != null)
				{
					acctService.addAccountingRecord(
						context.getCallingContext(), context.getBESEPI(),
						null, null, null, newCmdLine,
						results.exitCode(),
						results.userTime(), results.kernelTime(),
						results.wallclockTime(), results.maximumRSS());
				}
			}
			
			appendStandardError(history, stderrFile);
		}
		catch (InterruptedException ie)
		{
			synchronized(_processLock)
			{
				history.debug("Process Interrupted - Killing");
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
		HistoryContext history = HistoryContextFactory.createContext(
			HistoryEventCategory.Terminating);
		
		history.info("Terminating Activity Per Request");
		
		synchronized(_processLock)
		{
			_hardTerminate = Boolean.TRUE;
			_countAsFailedAttempt = countAsFailedAttempt;
			destroyProcess(_process);
		}
	}
}
