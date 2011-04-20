package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.File;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.factory.ActivityStateEnumeration;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.bes.BESConstructionParameters;
import edu.virginia.vcgr.genii.client.bes.ExitCondition;
import edu.virginia.vcgr.genii.client.bes.NormalExit;
import edu.virginia.vcgr.genii.client.bes.SignaledExit;
import edu.virginia.vcgr.genii.client.bes.Signals;
import edu.virginia.vcgr.genii.client.history.HistoryEventCategory;
import edu.virginia.vcgr.genii.client.nativeq.ApplicationDescription;
import edu.virginia.vcgr.genii.client.nativeq.JobToken;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueueConfiguration;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueueConnection;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueueException;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueueState;
import edu.virginia.vcgr.genii.client.pwrapper.ExitResults;
import edu.virginia.vcgr.genii.client.pwrapper.ProcessWrapper;
import edu.virginia.vcgr.genii.client.pwrapper.ProcessWrapperException;
import edu.virginia.vcgr.genii.client.pwrapper.ResourceUsageDirectory;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionException;
import edu.virginia.vcgr.genii.container.bes.execution.IgnoreableFault;
import edu.virginia.vcgr.genii.container.bes.execution.TerminateableExecutionPhase;
import edu.virginia.vcgr.genii.container.bes.jsdl.personality.common.BESWorkingDirectory;
import edu.virginia.vcgr.genii.container.bes.jsdl.personality.common.ResourceConstraints;
import edu.virginia.vcgr.genii.container.cservices.ContainerServices;
import edu.virginia.vcgr.genii.container.cservices.accounting.AccountingService;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContext;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContextFactory;
import edu.virginia.vcgr.jsdl.OperatingSystemNames;
import edu.virginia.vcgr.jsdl.ProcessorArchitecture;

public class QueueProcessPhase extends AbstractRunProcessPhase 
	implements TerminateableExecutionPhase
{
	static final long serialVersionUID = 0L;
	
	static private Log _logger = LogFactory.getLog(QueueProcessPhase.class);
	
	static private final String JOB_TOKEN_PROPERTY = 
		"edu.virginia.vcgr.genii.container.bes.phases.queue.job-token";
	static private final long DEFAULT_LOOP_CYCLE = 1000L * 10;
	
	private String _phaseShiftLock = new String();
	
	transient private NativeQueueState _state = null;
	transient private BESWorkingDirectory _workingDirectory = null;
	
	private File _fuseMountPoint;
	private URI _spmdVariation;
	private Integer _numProcesses;
	private Integer _numProcessesPerHost;
	private File _executable;
	private Collection<String> _arguments;
	private File _stdin;
	private File _stdout;
	private File _stderr;
	private Map<String, String> _environment;
	
	private ResourceConstraints _resourceConstraints;
	
	transient private JobToken _jobToken = null;
	transient private Boolean _terminate = null;
	
	public QueueProcessPhase(File fuseMountPoint,
		URI spmdVariation, Integer numProcesses,
		Integer numProcessesPerHost, File executable, 
		Collection<String> arguments, Map<String, String> environment,
		File stdin, File stdout, File stderr, 
		BESConstructionParameters constructionParameters, 
		ResourceConstraints resourceConstraints)
	{
		super(new ActivityState(
			ActivityStateEnumeration.Running, "Enqueing", false),
			constructionParameters);
	
		_fuseMountPoint = fuseMountPoint;
		_spmdVariation = spmdVariation;
		_numProcesses = numProcesses;
		_numProcessesPerHost = numProcessesPerHost;
		_executable = executable;
		_arguments = arguments;
		_environment = environment;
		_stdin = stdin;
		_stdout = stdout;
		_stderr = stderr;
		_resourceConstraints = resourceConstraints;
	}
	
	@Override
	public void terminate(boolean countAsFailedAttempt)
		throws ExecutionException
	{
		HistoryContext history = HistoryContextFactory.createContext(
			HistoryEventCategory.Terminating);
		
		history.createTraceWriter("BES Terminating Job").format(
			"The BES was asked to terminate the job (countAsFailedAttempt = %s)",
			countAsFailedAttempt).close();
		
		try
		{
			synchronized(_phaseShiftLock)
			{
				if (_workingDirectory == null || _jobToken == null)
				{
					_terminate = Boolean.TRUE;
					return;
				}
				
				NativeQueueConnection queue = connectQueue(
					_workingDirectory.getWorkingDirectory());
				history.trace("Asking Batch System (%s) to Cancel", queue);
				_logger.info(String.format(
					"Asking batch system (%s) to cancel the job.", queue));
				queue.cancel(_jobToken);
				
				_phaseShiftLock.notifyAll();
			}
		}
		catch (NativeQueueException nqe)
		{
			history.error(nqe, "Unable to Cancel Job");
			throw new ExecutionException("Unable to cancel job in queue.", 
				nqe);
		}
	}

	@Override
	public void execute(ExecutionContext context) throws Throwable
	{
		String stderrPath = null;
		File resourceUsageFile = null;
		
		HistoryContext history = HistoryContextFactory.createContext(
			HistoryEventCategory.CreatingActivity);
		
		if (_environment == null)
			_environment = new HashMap<String, String>();
		
		setExportedEnvironment(_environment);
		history.createDebugWriter("Activity Environment Set").format(
			"Activity environment set to %s", _environment).close();
		
		synchronized(_phaseShiftLock)
		{
			if (_terminate != null && _terminate.booleanValue())
			{
				history.debug("Activity Terminate Early");
				return;
			}
			
			_workingDirectory = context.getCurrentWorkingDirectory();
			NativeQueueConnection queue = connectQueue(
				_workingDirectory.getWorkingDirectory());
			
			_jobToken = (JobToken)context.getProperty(JOB_TOKEN_PROPERTY);
			if (_jobToken == null)
			{
				if (_environment != null)
				{
					String ogrshConfig = _environment.get("OGRSH_CONFIG");
					if (ogrshConfig != null)
					{
						File f = new File(
							context.getCurrentWorkingDirectory(
								).getWorkingDirectory(), 
							ogrshConfig);
						_environment.put("OGRSH_CONFIG", f.getAbsolutePath());
					}
				}
				
				_logger.info(String.format(
					"Asking batch system (%s) to submit the job.", queue));
				history.trace("Batch System (%s) Starting Activity", queue);
				resourceUsageFile = new ResourceUsageDirectory(_workingDirectory.getWorkingDirectory(
					)).getNewResourceUsageFile();
				preDelay();
				stderrPath = fileToPath(_stderr,
					_workingDirectory.getWorkingDirectory());
				PrintWriter hWriter = history.createInfoWriter(
					"Queue BES Submitting Activity").format(
						"BES submitting job to batch system:  ");
				hWriter.print(_executable.getAbsolutePath());
				for (String arg : _arguments)
					hWriter.format(" %s", arg);
				hWriter.close();
				
				_jobToken = queue.submit(new ApplicationDescription(_fuseMountPoint,
					_spmdVariation, _numProcesses, _numProcessesPerHost, _executable.getAbsolutePath(),
					_arguments,
					_environment, fileToPath(_stdin, null),
					fileToPath(_stdout, null), stderrPath, _resourceConstraints,
					resourceUsageFile));
				
				_logger.info(String.format(
					"Queue submitted job %s using command line:\n\t%s", 
					_jobToken, _jobToken.getCmdLine()));
				history.createTraceWriter("Job Queued into Batch System").format(
					"BES submitted job %s using command line:\n\t%s",
					_jobToken, _jobToken.getCmdLine()).close();
				
				context.setProperty(JOB_TOKEN_PROPERTY, _jobToken);
			}
			
			String lastState = null;
			while (true)
			{
				_state = queue.getStatus(_jobToken);
				context.updateState(new ActivityState(
					ActivityStateEnumeration.Running, 
					_state.toString(), false));
				if (lastState == null || !lastState.equals(_state.toString()))
				{
					history.trace("Batch System State:  %s", _state);
					lastState = _state.toString();
				}
				
				if (_state.isFinalState())
					break;
				
				_phaseShiftLock.wait(DEFAULT_LOOP_CYCLE);
			}
			context.setProperty(JOB_TOKEN_PROPERTY, null);
			postDelay();
			
			int exitCode = queue.getExitCode(_jobToken);
			history.info("Job Exited with Exit Code %d", exitCode);
			
			if (resourceUsageFile != null)
			{
				try
				{
					ExitResults eResults = ProcessWrapper.readResults(
						resourceUsageFile);
					exitCode = eResults.exitCode();
					
					AccountingService acctService =
						ContainerServices.findService(AccountingService.class);
					if (acctService != null)
					{
						OperatingSystemNames osName = 
							_constructionParameters.getResourceOverrides(
								).operatingSystemName();
						
						ProcessorArchitecture arch = 
							_constructionParameters.getResourceOverrides(
								).cpuArchitecture();
						
						Vector<String> command = new Vector<String>(
							_arguments);
						command.add(0, _executable.getAbsolutePath());
						acctService.addAccountingRecord(
							context.getCallingContext(), context.getBESEPI(),
							arch, osName, null, _jobToken.getCmdLine(), exitCode,
							eResults.userTime(), eResults.kernelTime(),
							eResults.wallclockTime(), eResults.maximumRSS());
					}
					
				}
				catch (ProcessWrapperException pwe)
				{
					history.warn(pwe, "Error Acquiring Accounting Info");
					throw new IgnoreableFault(
						"Error trying to read resource usage information.",
						pwe);
				}
			}
			
			ExitCondition exitCondition = interpretExitCode(exitCode);
			_logger.info(String.format("Process exited with %s.",
				(exitCondition instanceof SignaledExit) ?
					("Signal " + exitCondition) :
					("Exit code " + exitCondition)));
			if (exitCode == 257)
				throw new IgnoreableFault(
					"Queue process exited with signal.");
		}
		
		appendStandardError(history, stderrPath);
	}
	
	static private ExitCondition interpretExitCode(int exitCode)
	{
		if (exitCode > 128)
		{
			int index = exitCode - 128 - 1;
			if (index < 0 || index >= Signals.values().length)
				return new NormalExit(exitCode);
			
			return new SignaledExit(Signals.values()[exitCode - 128 - 1]);
		}
		
		return new NormalExit(exitCode);
	}
	
	@Override
	public ActivityState getPhaseState()
	{
		synchronized(_phaseShiftLock)
		{
			if (_state == null)
				return super.getPhaseState();
			
			return new ActivityState(ActivityStateEnumeration.Running,
				_state.toString(), false);
		}
	}
	
	private NativeQueueConnection connectQueue(File workingDirectory)
	{
		if (workingDirectory == null)
			throw new IllegalArgumentException("Working directory cannot be null.");
		
		try
		{
			NativeQueueConfiguration conf = 
			_constructionParameters.getNativeQueueConfiguration();
			if (conf == null)
				throw new RuntimeException(
					"Unable to acquire connection to native queue -- no queue defined.");
			
			return conf.connect(
				_constructionParameters.getResourceOverrides(),
				_constructionParameters.getCmdLineManipulatorConfiguration(),
				workingDirectory);
		}
		catch (NativeQueueException nqe)
		{
			throw new RuntimeException(
				"Unable to acquire connection to native queue.", nqe);
		}
	}
}
