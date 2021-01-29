package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import edu.virginia.vcgr.genii.container.bes.activity.BESActivity;
import edu.virginia.vcgr.genii.container.bes.execution.IgnoreableFault;
import edu.virginia.vcgr.genii.container.bes.execution.TerminateableExecutionPhase;
import edu.virginia.vcgr.genii.container.cservices.ContainerServices;
import edu.virginia.vcgr.genii.container.cservices.accounting.AccountingService;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContext;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContextFactory;
import edu.virginia.vcgr.genii.container.exportdir.GffsExportConfiguration;
import edu.virginia.vcgr.genii.security.credentials.CredentialWallet;
import edu.virginia.vcgr.jsdl.GPUProcessorArchitecture;
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
	private Double _memory;
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

	
	public QueueProcessPhase(File fuseMountPoint, URI spmdVariation, Double memory, Integer numProcesses, Integer numProcessesPerHost,
		Integer threadsPerProcess, File executable, Collection<String> arguments, Map<String, String> environment, File stdin, File stdout,
		File stderr, BESConstructionParameters constructionParameters, ResourceConstraints resourceConstraints)
	{
		super(new ActivityState(ActivityStateEnumeration.Running, "Enqueing"), constructionParameters);

		_fuseMountPoint = fuseMountPoint;
		_spmdVariation = spmdVariation;
		 _memory=memory;
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

				_terminate = Boolean.TRUE;

				_phaseShiftLock.notifyAll();
			}
		} catch (NativeQueueException nqe) {
			history.error(nqe, "Unable to Cancel Job");
			throw new ExecutionException("Unable to cancel job in queue.", nqe);
		}
	}

	@Override
	public void execute(ExecutionContext context, BESActivity activity) throws Throwable
	{
		String stderrPath = null;
		File resourceUsageFile;
		// 2019-04-04 ASG. Added to handle jobs disappearing from the queu. We're going to make them as complete for now.
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
		
		// CCH 2020 June 24
		// POSIX Applications don't set these fields and cause a null pointer exception later if we don't set defaults
		if (_numProcesses == null)
			_numProcesses = new Integer(1);
		if (_numProcessesPerHost == null)
			_numProcessesPerHost = new Integer(1);
		if (_threadsPerProcess == null) 
			_threadsPerProcess = new Integer(1);

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
				// old code
				// File resourceUsageFile = new ResourceUsageDirectory(workingDirectory).getNewResourceUsageFile();
				// 2020-04-18 by ASG during coronovirus to put the accounting stuff in place
				// This next call establishes both the job working directory, JWD, and the Accounting directory for the file.
				//resourceUsageFile = new ResourceUsageDirectory(_workingDirectory.getWorkingDirectory()).getNewResourceUsageFile();
				ResourceUsageDirectory tmp=new ResourceUsageDirectory(_workingDirectory.getWorkingDirectory());
                // 2020-05-01 ASG Add a HOSTNAME file
                File jwd=tmp.getJWD();
                File hostName=new File(jwd,"JOBNAME");
                if (hostName.createNewFile()) // Create the HOSTNAME file
                {                             
                	try {
                		FileWriter myWriter = new FileWriter(hostName);
                		myWriter.write(activity.getJobName()+"\n");
                		myWriter.close();
                	} catch (IOException e) {
                		System.out.println("An error occurred writting the HOSTNAME file.");
                		e.printStackTrace();
                	}
                }
                // End jobName updates			
                
                resourceUsageFile =tmp.getNewResourceUsageFile();  // This should point to the accounting directory, not create a properties file.
				if (_logger.isDebugEnabled()) {
					_logger.debug("Generate Properties Constructor tmp: " + tmp);
					_logger.debug("Generate Properties Constructor userName: " + userName);
					_logger.debug("Generate Properties Constructor _executable (File): " + _executable);
					_logger.debug("Generate Properties Constructor _executable.getAbsolutePath (String): " + _executable.getAbsolutePath());
					_logger.debug("Generate Properties Constructor _memory: " + _memory);
					_logger.debug("Generate Properties Constructor _numProcesses: " + _numProcesses);
					_logger.debug("Generate Properties Constructor _numProcessesPerHost: " + _numProcessesPerHost);
					_logger.debug("Generate Properties Constructor _threadsPerProcess: " + _threadsPerProcess);
					_logger.debug("Generate Properties Constructor _jobName: " + activity.getJobName());
				}
				generateProperties(tmp,userName,_executable.getAbsolutePath(), _memory, _numProcesses,
						_numProcessesPerHost, _threadsPerProcess, activity);
				generateJSDL(tmp, activity);

				// End of updates 2020-04-18
				
				String execName = _executable.getAbsolutePath();
				Vector<String> args = new Vector<String>(_arguments);
				
				// CCH 2020 October 14
				if (execName.endsWith(".simg") || execName.endsWith(".sif") || execName.endsWith(".qcow2"))
				{			
					if (_logger.isDebugEnabled())
						_logger.debug("Handling image executable (.simg or .qcow2): " + execName);
					String[] execNameArray = execName.split("/");
					// 2020 June 09 by CCH
					// Turns out that the executable path is resolved at this point.
					// Even though I could only give test.simg, the execName is the full path: /nfs/.../<ticket>/test.simg.
					// As opposed to just test.simg. This wasn't a problem before, but we indicate Lancium images with Lancium/<lancium_image>.simg. 
					// To check for Lancium images, we check for the second-to-last token and see if it equals Lancium.
					boolean usingLanciumImage = false;
					if (execNameArray.length >= 2)
						usingLanciumImage = execNameArray[execNameArray.length-2].equals("Lancium");
					if (_logger.isDebugEnabled()) {
						_logger.debug("Second to last element in execNameArray: " + execNameArray[execNameArray.length-2]);
						_logger.debug("Using Lancium image? " + usingLanciumImage);
					}
					execName = execNameArray[execNameArray.length-1];
					String lanciumEnvironment = activity.getLanciumEnvironment();
					boolean developmentNamespace = lanciumEnvironment != null && lanciumEnvironment.equals("Development");
					if (_logger.isDebugEnabled())
						_logger.debug("developmentNamespace: " + developmentNamespace);
					// 2020-11-04 by ASG. This code did not work, updated
					// String imagePath = usingLanciumImage ? "../Images/Lancium/" : "../Images/" + (developmentNamespace ? "development/" : "") + userName + "/" + execName;
					String imagePath=null;
					if (usingLanciumImage) {
						imagePath="../Images/Lancium/" + execName;
					}
					else {
						imagePath="../Images/" + (developmentNamespace ? "development/" : "") + userName + "/" + execName;
					}
					// This should use getContainerProperty job BES directory
					if (_logger.isDebugEnabled())
						_logger.debug("imagePath: " + imagePath);
					if (imagePath.endsWith(".qcow2")) {
						execName = jwd.getAbsolutePath() + "/" + "../vmwrapper.sh";
					}
					String fullPath = jwd.getAbsolutePath()+ "/" + imagePath;
					if (_logger.isDebugEnabled())
						_logger.debug("Handling image executable: " + fullPath);

					if (Files.exists(Paths.get(fullPath))) {
						String MIME = Files.probeContentType(Paths.get(fullPath));
						if (_logger.isDebugEnabled())
							_logger.debug(fullPath + " exists with MIME type: " + MIME);
						if (MIME.contains("QEMU QCOW Image") || MIME.contains("run-singularity script executable")|| MIME.contains("model/x.stl-binary")) {
							if (_logger.isDebugEnabled())
								_logger.debug(fullPath + " exists and has the correct MIME type.");
						}
						else {
							if (_logger.isDebugEnabled())
								_logger.debug(fullPath + " exists but has the wrong MIME type.");
							// 2020 May 27 CCH, not sure how to handle bad MIME type
							// Currently, set imagePath to something completely different so the original image does not execute
							imagePath = "../Images/Lancium/BAD_MIME";
						}
					}
					else {
						if (_logger.isDebugEnabled())
							_logger.debug(fullPath + " does not exist.");
					}
					
					args.add(0, imagePath);
				}
				// End of "if qcow2 or singularity

				preDelay();
				stderrPath = fileToPath(_stderr, null);
				PrintWriter hWriter =
						history.createInfoWriter("Queue BES Submitting Activity").format("BES submitting job to batch system:  ");
				hWriter.print(_executable.getAbsolutePath());
				for (String arg : _arguments)
					hWriter.format(" %s", arg);
				hWriter.close();

			/*	_jobToken = queue.submit(new ApplicationDescription(_fuseMountPoint, _spmdVariation, _numProcesses, _numProcessesPerHost,
						_threadsPerProcess, _executable.getAbsolutePath(), args, _environment, fileToPath(_stdin, null),
						fileToPath(_stdout, null), stderrPath, _resourceConstraints, resourceUsageFile));
			*/
				_jobToken = queue.submit(new ApplicationDescription(_fuseMountPoint, _spmdVariation, _numProcesses, _numProcessesPerHost,
						_threadsPerProcess, execName, args, _environment, fileToPath(_stdin, null),
						fileToPath(_stdout, null), stderrPath, _resourceConstraints, resourceUsageFile, activity.hasBeenRestartedFromCheckpoint()));

				_logger.info(String.format("Queue submitted job '%s' for userID '%s' using command line:\n\t%s", _jobToken, userName,
						_jobToken.getCmdLine()));
				history.createTraceWriter("Job Queued into Batch System")
				.format("BES submitted job %s using command line:\n\t%s", _jobToken, _jobToken.getCmdLine()).close();

				context.setProperty(JOB_TOKEN_PROPERTY, _jobToken);
			}
			else
			{
				ResourceUsageDirectory tmp=new ResourceUsageDirectory(_workingDirectory.getWorkingDirectory());
				resourceUsageFile =tmp.getResourceUsageFile();  // This should point to the accounting directory, not create a properties file.
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
					context.updateState(new ActivityState(ActivityStateEnumeration.Running, _state.toString()));
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
			while (secondsWaited <= delay) {				
				try {
					exitCode = queue.getExitCode(_jobToken);
					break;
				} catch (QueueResultsException | NativeQueueException  exe) {
					if (secondsWaited==delay){
						// ASG 2019-01-13 Ok, if we get here we have been unable to get queue.script.result. That most likely means it disappeared and did not exit.
						// This can happen if the job is terminated by the scheduling system, or the node died. So we want to throw an exception, though not
						// necessarily a fatal one.
						// 2019-04-04 ASG. So this turns out to be a mistake. It turns jobs that were terminated due to node failure and time limit terminations into job
						// failures. Particularly the time limit excepts are a problem, since the job will be marked as failed and they will be restarted ....
						// What we want is to be able to talk to the queue manager accounting system .. that is not possible right now on most resources. When it is 
						// available we will pick it up in getExitCode.
						jobDisapparedFromQueue=true;
						exitCode=143;
						context.updateState(new ActivityState(ActivityStateEnumeration.Running, _state.toString()));
						if (lastState == null || !lastState.equals(_state.toString())) {
							if (_logger.isDebugEnabled())
								_logger.debug("queue job '" + _jobToken.toString() + "' updated to state: " + _state);
							history.trace("Batch System State:  %s", _state);
							lastState = _state.toString();
						}
					}
				}	catch (IOException ioe) {
					// See comments for catch above, they are the same
					jobDisapparedFromQueue=true;
					exitCode=143;
					context.updateState(new ActivityState(ActivityStateEnumeration.Running, _state.toString()));
					if (lastState == null || !lastState.equals(_state.toString())) {
						if (_logger.isDebugEnabled())
							_logger.debug("queue job '" + _jobToken.toString() + "' updated to state: " + _state);
						history.trace("Batch System State:  %s", _state);
						lastState = _state.toString();
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
					ExitResults eResults;
					try
					{
						eResults = ProcessWrapper.readResults(resourceUsageFile);
					}
					catch (ProcessWrapperException e)
					{
						// LAK 2020 Aug 12: Since early termination no longer short circuits job execution, we can have the case where the rusage.json file does not exist.
						// This will happen when the job is terminated before the job starts execution.
						// In this case, we set the exit code simply to 143 and default ExitResults -
						// this is consistent with the exit code that the pwrapper would have set if it had run and terminated before job execution
						// (besides the "None" processor tag).
						_logger.debug("No exit results found, generating default values");
						eResults = new ExitResults(143, 0L, 0L, 0L, 0L, "None");
					}

					exitCode = eResults.exitCode();

					AccountingService acctService = ContainerServices.findService(AccountingService.class);
					if (acctService != null) {
						OperatingSystemNames osName = _constructionParameters.getResourceOverrides().operatingSystemName();

						ProcessorArchitecture arch = _constructionParameters.getResourceOverrides().cpuArchitecture();
						GPUProcessorArchitecture gpuarch = _constructionParameters.getResourceOverrides().gpuArchitecture();

						Vector<String> command = new Vector<String>(_arguments);
						command.add(0, _executable.getAbsolutePath());
						if (_numProcesses ==null) _numProcesses = new Integer(1);
						eResults.wallclockTime().setValue(eResults.wallclockTime().value() * _numProcesses);
						acctService.addAccountingRecord(context.getCallingContext(), context.getBESEPI(), arch, osName, null,
								_jobToken.getCmdLine(), exitCode, eResults.userTime(), eResults.kernelTime(), eResults.wallclockTime(),
								eResults.maximumRSS(),_numProcesses);
						//	history.info("Job wallclocktime is: " + eResults.wallclockTime().toString() + " and the job executed with %d procesoors", _numProcesses);
					}

				} catch (Exception e) {
					history.warn(e, "Error While Handling Accounting Info");
					throw new IgnoreableFault("Unhandled error trying to handle resource usage information.", e);
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

			return new ActivityState(ActivityStateEnumeration.Running, _state.toString());
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

	@Override
	public void notifyPwrapperIsTerminating() {
		NativeQueueConnection queue = connectQueue(_workingDirectory.getWorkingDirectory());
		try {
			queue.updateStatusToTerminated(_jobToken);
		} catch (NativeQueueException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
