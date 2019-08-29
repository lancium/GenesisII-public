package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.factory.ActivityStateEnumeration;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.bes.BESConstructionParameters;
import edu.virginia.vcgr.genii.client.bes.ExecutionContext;
import edu.virginia.vcgr.genii.client.bes.ExecutionException;
import edu.virginia.vcgr.genii.client.bes.ExitCondition;
import edu.virginia.vcgr.genii.client.bes.NormalExit;
import edu.virginia.vcgr.genii.client.bes.SignaledExit;
import edu.virginia.vcgr.genii.client.bes.Signals;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.history.HistoryEventCategory;
import edu.virginia.vcgr.genii.client.jsdl.personality.common.BESWorkingDirectory;
import edu.virginia.vcgr.genii.client.jsdl.personality.common.ResourceConstraints;
import edu.virginia.vcgr.genii.client.nativeq.ApplicationDescription;
import edu.virginia.vcgr.genii.client.nativeq.JobToken;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueueConfiguration;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueueConnection;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueueException;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueueState;
import edu.virginia.vcgr.genii.client.nativeq.QueueResultsException;
import edu.virginia.vcgr.genii.client.pwrapper.ExitResults;
import edu.virginia.vcgr.genii.client.pwrapper.ProcessWrapper;
import edu.virginia.vcgr.genii.client.pwrapper.ProcessWrapperException;
import edu.virginia.vcgr.genii.client.pwrapper.ResourceUsageDirectory;
import edu.virginia.vcgr.genii.client.security.PreferredIdentity;
import edu.virginia.vcgr.genii.client.utils.units.Duration;
import edu.virginia.vcgr.genii.client.utils.units.DurationUnits;
import edu.virginia.vcgr.genii.container.bes.execution.IgnoreableFault;
import edu.virginia.vcgr.genii.container.bes.execution.TerminateableExecutionPhase;
import edu.virginia.vcgr.genii.container.cservices.ContainerServices;
import edu.virginia.vcgr.genii.container.cservices.accounting.AccountingService;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContext;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContextFactory;
import edu.virginia.vcgr.genii.container.exportdir.GffsExportConfiguration;
import edu.virginia.vcgr.genii.security.credentials.CredentialWallet;
import edu.virginia.vcgr.jsdl.OperatingSystemNames;
import edu.virginia.vcgr.jsdl.ProcessorArchitecture;

public class QueueProcessPhase extends AbstractRunProcessPhase implements TerminateableExecutionPhase
{
	static final long serialVersionUID = 0L;

	static private Log _logger = LogFactory.getLog(QueueProcessPhase.class);

	static private final String JOB_TOKEN_PROPERTY = "edu.virginia.vcgr.genii.container.bes.phases.queue.job-token";
	static private final long DEFAULT_LOOP_CYCLE = 1000L * 10;

	private String _phaseShiftLock = new String();

	transient private NativeQueueState _state = null;
	transient private BESWorkingDirectory _workingDirectory = null;

	private File _fuseMountPoint;
	private URI _spmdVariation;
	private Integer _numProcesses;
	private Integer _numProcessesPerHost;
	private Integer _threadsPerProcess;
	private File _executable;
	private Collection<String> _arguments;
	private File _stdin;
	private File _stdout;
	private File _stderr;
	private Map<String, String> _environment;

	private ResourceConstraints _resourceConstraints;

	transient private JobToken _jobToken = null;
	transient private Boolean _terminate = null;

	public QueueProcessPhase(File fuseMountPoint, URI spmdVariation, Integer numProcesses, Integer numProcessesPerHost,
		Integer threadsPerProcess, File executable, Collection<String> arguments, Map<String, String> environment, File stdin, File stdout,
		File stderr, BESConstructionParameters constructionParameters, ResourceConstraints resourceConstraints)
	{
		super(new ActivityState(ActivityStateEnumeration.Running, "Enqueing", false), constructionParameters);

		_fuseMountPoint = fuseMountPoint;
		_spmdVariation = spmdVariation;
		_numProcesses = numProcesses;
		_numProcessesPerHost = numProcessesPerHost;
		_threadsPerProcess = threadsPerProcess;
		_executable = executable;
		_arguments = arguments;
		_environment = environment;
		_stdin = stdin;
		_stdout = stdout;
		_stderr = stderr;
		_resourceConstraints = resourceConstraints;
	}

	@Override
	public void terminate(boolean countAsFailedAttempt) throws ExecutionException
	{
		HistoryContext history = HistoryContextFactory.createContext(HistoryEventCategory.Terminating);

		history.createTraceWriter("BES Terminating Job")
			.format("The BES was asked to terminate the job (countAsFailedAttempt = %s)", countAsFailedAttempt).close();

		try {
			synchronized (_phaseShiftLock) {
				if (_workingDirectory == null || _jobToken == null) {
					_terminate = Boolean.TRUE;
					return;
				}

				NativeQueueConnection queue = connectQueue(_workingDirectory.getWorkingDirectory());
				history.trace("Asking Batch System (%s) to Cancel", queue);
				_logger.info(String.format("Asking batch system (%s) to cancel the job.", queue));
				queue.cancel(_jobToken);

				_phaseShiftLock.notifyAll();
			}
		} catch (NativeQueueException nqe) {
			history.error(nqe, "Unable to Cancel Job");
			throw new ExecutionException("Unable to cancel job in queue.", nqe);
		}
	}

	@Override
	public void execute(ExecutionContext context) throws Throwable
	{
		String stderrPath = null;
		File resourceUsageFile = null;
		// 2019-04-04 ASG. Added to handle jobs disappearing from the queue. We're going to make them as complete for now.
		boolean jobDisapparedFromQueue=false;

		HistoryContext history = HistoryContextFactory.createContext(HistoryEventCategory.CreatingActivity);

		if (_environment == null)
			_environment = new HashMap<String, String>();

		setExportedEnvironment(_environment);
		history.createDebugWriter("Activity Environment Set").format("Activity environment set to %s", _environment).close();

		// ASG 2015-11-05. Updated to get a nice log message
		ICallingContext callContext = context.getCallingContext();

		String prefId = (PreferredIdentity.getCurrent() != null ? PreferredIdentity.getCurrent().getIdentityString() : null);
		X509Certificate owner = GffsExportConfiguration.findPreferredIdentityServerSide(callContext, prefId);
		String userName = CredentialWallet.extractUsername(owner);
		if (userName == null)
			userName = "UnKnown";

		// End of updates

		synchronized (_phaseShiftLock) {
			if (_terminate != null && _terminate.booleanValue()) {
				history.debug("Activity Terminate Early");
				return;
			}

			_workingDirectory = context.getCurrentWorkingDirectory();
			NativeQueueConnection queue = connectQueue(_workingDirectory.getWorkingDirectory());

			_jobToken = (JobToken) context.getProperty(JOB_TOKEN_PROPERTY);
			if (_jobToken == null) {
				if (_environment != null) {
					String ogrshConfig = _environment.get("OGRSH_CONFIG");
					if (ogrshConfig != null) {
						File f = new File(context.getCurrentWorkingDirectory().getWorkingDirectory(), ogrshConfig);
						_environment.put("OGRSH_CONFIG", f.getAbsolutePath());
					}
				}

				_logger.info(String.format("Asking batch system (%s) to submit the job.", queue));
				history.trace("Batch System (%s) Starting Activity", queue);
				resourceUsageFile = new ResourceUsageDirectory(_workingDirectory.getWorkingDirectory()).getNewResourceUsageFile();
				preDelay();
				stderrPath = fileToPath(_stderr, null);
				PrintWriter hWriter =
					history.createInfoWriter("Queue BES Submitting Activity").format("BES submitting job to batch system:  ");
				hWriter.print(_executable.getAbsolutePath());
				for (String arg : _arguments)
					hWriter.format(" %s", arg);
				hWriter.close();
				try {
				_jobToken = queue.submit(new ApplicationDescription(_fuseMountPoint, _spmdVariation, _numProcesses, _numProcessesPerHost,
					_threadsPerProcess, _executable.getAbsolutePath(), _arguments, _environment, fileToPath(_stdin, null),
					fileToPath(_stdout, null), stderrPath, _resourceConstraints, resourceUsageFile));
				// 2019-08-28 by ASg .. submit can throw a fault we do not catch - NativeQueueException .. if it cannot qsub
				}
				catch (NativeQueueException er) {
					// Ok, the submit did not work. Usually means that the paths don't have the right permission or the queue is down
					// This is not something we can easily handle.
					String error=er.getMessage();
					_logger.error(String.format("Unable to submit job to the local queue. Submitted job '%s' for userID '%s' using command line:\n\t%s", _jobToken, userName,
					_jobToken.getCmdLine()));
					// Now what?
					context.updateState(new ActivityState(ActivityStateEnumeration.Failed, _state.toString(), false));
				}
				_logger.info(String.format("Queue submitted job '%s' for userID '%s' using command line:\n\t%s", _jobToken, userName,
					_jobToken.getCmdLine()));
				history.createTraceWriter("Job Queued into Batch System")
					.format("BES submitted job %s using command line:\n\t%s", _jobToken, _jobToken.getCmdLine()).close();

				context.setProperty(JOB_TOKEN_PROPERTY, _jobToken);
			}
			
			String lastState = null;
			while (true) {
				boolean stateIsUsable = false;
				try {
					_state = queue.getStatus(_jobToken);
					stateIsUsable = true;
				} catch (Exception e) {
					_logger.error("caught exception while asking for queue state; ignoring result", e);
				}
				if (stateIsUsable) {
					context.updateState(new ActivityState(ActivityStateEnumeration.Running, _state.toString(), false));
					if (lastState == null || !lastState.equals(_state.toString())) {
						if (_logger.isDebugEnabled())
							_logger.debug("queue job '" + _jobToken.toString() + "' updated to state: " + _state);
						history.trace("Batch System State:  %s", _state);
						lastState = _state.toString();
					}
				}

				// the state may have been updated elsewhere, so we still examine if we're in final state here.
				if (_state.isFinalState()) {
					if (_logger.isDebugEnabled())
						_logger.debug("queue job '" + _jobToken.toString() + "' is now in a final state: " + _state);
					break;
				}

				_phaseShiftLock.wait(DEFAULT_LOOP_CYCLE);
			}
			context.setProperty(JOB_TOKEN_PROPERTY, null);
			// =========================================================
			// 2016-09-07 New code by ASG to probe every second until the post execution delay has passed rather than waiting for that long.
			// First set up the delay; default to 10 seconds if not set
			int delay=10;
			int secondsWaited=0;
			if (_constructionParameters != null) {
				Duration postDelay = _constructionParameters.postExecutionDelay();
				if (postDelay != null) {
					delay=(int) postDelay.as(DurationUnits.Seconds);

				}
			}
			int exitCode=0;
			// Wait until the data is there or time is expired.

			while (secondsWaited < delay) {			
				/*
				 *2019-08-22 by ASG. Massive update and simplification of logic. Now if we cannot find queue state result, we assume 250, the
				 * job was terminated by the queueing system --- for some sort of resource problem: wallClock limit, memoryLimit,
				 * cpu/thread limit. We many also have been canceled by our power management system. getExit code will determine that by
				 * looking for a flag in the directory and setting exitcode 251.
				 */

				exitCode = queue.getExitCode(_jobToken);
				if (_logger.isDebugEnabled())
					_logger.debug("queue job '" + _jobToken.toString() + "' exit code is: " + exitCode);
				// getExitCode returns a 250 if the result file is not there, and a 251 if there is a flag set 
				// indicating we powered down the nodes the job was running on.
				// So if we get a 250, it means we have to wait until the delay has passed.
				if (exitCode==250 && secondsWaited==delay) {
					jobDisapparedFromQueue=true;
					context.updateState(new ActivityState(ActivityStateEnumeration.Finished, _state.toString(), false));
					if (lastState == null || !lastState.equals(_state.toString())) {
						if (_logger.isDebugEnabled())
							_logger.debug("queue job '" + _jobToken.toString() + "' updated to state: " + _state);
						history.trace("Batch System State:  %s", _state);
						lastState = _state.toString();
						break;
					}
				}
				if (exitCode==251) {
					// 2019-08-22 by ASG. We will figure out what to do with these jobs later. I'd like to requeue them.
				}
				if (exitCode<240) {
					context.updateState(new ActivityState(ActivityStateEnumeration.Finished, _state.toString(), false));
					if (lastState == null || !lastState.equals(_state.toString())) {
						if (_logger.isDebugEnabled())
							_logger.debug("queue job '" + _jobToken.toString() + "' updated to state: " + _state);
						history.trace("Batch System State:  %s", _state);
						lastState = _state.toString();
						break;
					}
				}
				Thread.sleep(1000);
				secondsWaited++;
			}
			//postDelay();
			//exitCode = queue.getExitCode(_jobToken);
			// End of 2016-09-07 ASG updates
			// **********************************************************
			
			history.info("Job Exited with Exit Code %d", exitCode);
			if (!jobDisapparedFromQueue && resourceUsageFile != null) {
				try {
					ExitResults eResults = ProcessWrapper.readResults(resourceUsageFile);
					exitCode = eResults.exitCode();

					AccountingService acctService = ContainerServices.findService(AccountingService.class);
					if (acctService != null) {
						OperatingSystemNames osName = _constructionParameters.getResourceOverrides().operatingSystemName();

						ProcessorArchitecture arch = _constructionParameters.getResourceOverrides().cpuArchitecture();

						Vector<String> command = new Vector<String>(_arguments);
						command.add(0, _executable.getAbsolutePath());
						if (_numProcesses ==null) _numProcesses = new Integer(1);
						eResults.wallclockTime().setValue(eResults.wallclockTime().value() * _numProcesses);
						acctService.addAccountingRecord(context.getCallingContext(), context.getBESEPI(), arch, osName, null,
							_jobToken.getCmdLine(), exitCode, eResults.userTime(), eResults.kernelTime(), eResults.wallclockTime(),
							eResults.maximumRSS(),_numProcesses);
					//	history.info("Job wallclocktime is: " + eResults.wallclockTime().toString() + " and the job executed with %d procesoors", _numProcesses);
					}

				} catch (ProcessWrapperException pwe) {
					history.warn(pwe, "Error Acquiring Accounting Info");
					throw new IgnoreableFault("Error trying to read resource usage information.", pwe);
				}
			}

			ExitCondition exitCondition = interpretExitCode(exitCode);
			_logger.info(String.format("Process exited with %s.",
				(exitCondition instanceof SignaledExit) ? ("Signal " + exitCondition) : ("Exit code " + exitCondition)));
			if (exitCode == 257)
				throw new IgnoreableFault("Queue process exited with signal.");
		}

		appendStandardError(history, stderrPath);
	}

	static private ExitCondition interpretExitCode(int exitCode)
	{
		if (exitCode > 128) {
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
		synchronized (_phaseShiftLock) {
			if (_state == null)
				return super.getPhaseState();

			return new ActivityState(ActivityStateEnumeration.Running, _state.toString(), false);
		}
	}

	private NativeQueueConnection connectQueue(File workingDirectory)
	{
		if (workingDirectory == null)
			throw new IllegalArgumentException("Working directory cannot be null.");

		try {
			NativeQueueConfiguration conf = _constructionParameters.getNativeQueueConfiguration();
			if (conf == null)
				throw new RuntimeException("Unable to acquire connection to native queue; no queue defined.");

			return conf.connect(_constructionParameters.getResourceOverrides(), _constructionParameters.getCmdLineManipulatorConfiguration(),
				workingDirectory);
		} catch (NativeQueueException nqe) {
			throw new RuntimeException("Unable to acquire connection to native queue.", nqe);
		}
	}
}
