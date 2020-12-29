package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;
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
import edu.virginia.vcgr.genii.client.bes.ExecutionContext;
import edu.virginia.vcgr.genii.client.bes.ExecutionException;
import edu.virginia.vcgr.genii.client.cmdLineManipulator.CmdLineManipulatorUtils;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.history.HistoryEventCategory;
import edu.virginia.vcgr.genii.client.pwrapper.ExitResults;
import edu.virginia.vcgr.genii.client.pwrapper.ProcessWrapper;
import edu.virginia.vcgr.genii.client.pwrapper.ProcessWrapperFactory;
import edu.virginia.vcgr.genii.client.pwrapper.ProcessWrapperToken;
import edu.virginia.vcgr.genii.client.pwrapper.ResourceUsageDirectory;
import edu.virginia.vcgr.genii.client.security.PreferredIdentity;
import edu.virginia.vcgr.genii.container.bes.activity.BESActivity;
import edu.virginia.vcgr.genii.container.bes.execution.ContinuableExecutionException;
import edu.virginia.vcgr.genii.container.bes.execution.TerminateableExecutionPhase;
import edu.virginia.vcgr.genii.container.cservices.ContainerServices;
import edu.virginia.vcgr.genii.container.cservices.accounting.AccountingService;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContext;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContextFactory;
import edu.virginia.vcgr.genii.container.exportdir.GffsExportConfiguration;
import edu.virginia.vcgr.genii.security.credentials.CredentialWallet;

public class RunProcessPhase extends AbstractRunProcessPhase implements TerminateableExecutionPhase, Serializable
{
	static final long serialVersionUID = 0L;

	static final private String EXECUTING_STAGE = "executing";

	static private Log _logger = LogFactory.getLog(RunProcessPhase.class);

	private URI _spmdVariation;
	private Double _memory;
	private Integer _numProcesses;
	private Integer _numProcessesPerHost;
	private Integer _threadsPerProcess;
	private File _fuseMountPoint;
	private File _commonDirectory;
	private File _executable;
	private String[] _arguments;
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

	public RunProcessPhase(File fuseMountPoint, URI spmdVariation, Double memory, Integer numProcesses, Integer numProcessesPerHost,
		Integer threadsPerProcess, File commonDirectory, File executable, String[] arguments, Map<String, String> environment,
		PassiveStreamRedirectionDescription redirects, BESConstructionParameters constructionParameters)
	{
		super(new ActivityState(ActivityStateEnumeration.Running, EXECUTING_STAGE), constructionParameters);

		_spmdVariation = spmdVariation;
		_memory=memory;
		_numProcesses = numProcesses;
		_numProcessesPerHost = numProcessesPerHost;
		_threadsPerProcess = threadsPerProcess;
		// 2020-04-21 by ASG. Need to set these to a default of 1
		if (numProcesses==null) {
			_numProcesses=new Integer(1);
		}
		if (numProcessesPerHost==null) {
			_numProcessesPerHost=new Integer(1);
		}
		if (threadsPerProcess==null) {
			_threadsPerProcess=new Integer(1);
		}
		if (memory==null) {
			_memory=new Double(2.0*1024.0*1024.0*1024.0*(new Double(_threadsPerProcess)));
		}
		_fuseMountPoint = fuseMountPoint;
		_commonDirectory = commonDirectory;

		if (executable == null)
			throw new IllegalArgumentException("Argument \"executable\" cannot be null.");
		if (arguments == null)
			arguments = new String[0];

		if (redirects == null)
			redirects = new PassiveStreamRedirectionDescription(null, null, null);

		_executable = executable;
		_arguments = arguments;
		_environment = environment;

		_redirects = redirects;
	}

	protected void finalize() throws Throwable
	{
		synchronized (_processLock) {
			if (_process != null)
				destroyProcess(_process);
		}
	}

	@Override
	public void execute(ExecutionContext context, BESActivity activity) throws Throwable
	{
		File stderrFile = null;
		List<String> command, newCmdLine;
		ProcessWrapperToken token;
		HistoryContext history = HistoryContextFactory.createContext(HistoryEventCategory.CreatingActivity);

		// ASG 2015-11-05. Updated to get a nice log message
		ICallingContext callContext = context.getCallingContext();

		String prefId = (PreferredIdentity.getCurrent() != null ? PreferredIdentity.getCurrent().getIdentityString() : null);
		X509Certificate owner = GffsExportConfiguration.findPreferredIdentityServerSide(callContext, prefId);
		String userName = CredentialWallet.extractUsername(owner);
		if (userName == null)
			userName = "UnKnown";

		// End of updates

		synchronized (_processLock) {
			File workingDirectory = context.getCurrentWorkingDirectory().getWorkingDirectory();
			command = new Vector<String>();

			// 2020 May 26 CCH, if executable is an image, we set the new executable to be the appropriate wrapper and push the image path into the arguments
			// To generate the image path, we use the preferred identity to fill in the path: ../Images/<identity>/<image>
			// Images can be in the form Lancium/<image> or just <image>. 
			// Currently, if it contains a slash we're only expected "Lancium", 
			// but in future we might have organizations so image paths could be <Organization>/<image>
			String execName = _executable.getAbsolutePath();
			if (execName.endsWith(".simg") || execName.endsWith(".sif") || execName.endsWith(".qcow2")) {
				// 2020 May 26 CCH, execName might be in the form Lancium/image.simg which is why we're splitting execName up here
				String[] execNameArray = execName.split("/");
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
				String imageDir = usingLanciumImage ? "../Images/Lancium/" : "../Images/" + (developmentNamespace ? "development/" : "") + userName + "/";
				if (_logger.isDebugEnabled())
					_logger.debug("Handling image executable (.sif/.simg or .qcow2)...");
					_logger.debug("Executable: " + execName + ", Username: " + userName + ", Image directory: " + imageDir);
				String imagePath = imageDir + execName;
				// This should use getContainerProperty job BES directory
				// Assigning the appropriate wrapper as the executable
				if (imagePath.endsWith(".qcow2")) {
					execName = "../vmwrapper.sh";
				}
				_executable = new File(workingDirectory, execName);
				if (_logger.isDebugEnabled())
					_logger.debug("Handling image executable: " + execName);
					_logger.debug("Adding executable: " + execName);
					_logger.debug("Adding path to image: " + imagePath);
				if (Files.exists(Paths.get(imagePath))) {
					String MIME = Files.probeContentType(Paths.get(imagePath));
					if (_logger.isDebugEnabled())
						_logger.debug(imagePath + " exists with MIME type: " + MIME);
					if (MIME.contains("QEMU QCOW Image") || MIME.contains("run-singularity script executable")) {
						if (_logger.isDebugEnabled())
							_logger.debug(imagePath + " exists and has the correct MIME type.");						}
					else {
						if (_logger.isDebugEnabled())
							_logger.debug(imagePath + " exists but has the wrong MIME type.");
						// 2020 May 27 CCH, not sure how to handle bad MIME type
						// Currently, set imagePath to something completely different so the original image does not execute
						imagePath = "../Images/Lancium/BAD_MIME";
					}
				}
				else {
					if (_logger.isDebugEnabled())
						_logger.debug(imagePath + " does not exist.");
				}
				command.add(execName);
				command.add(imagePath);
			}
			else {
				command.add(execName);
			}
			for (String arg : _arguments)
				command.add(arg);
			
			if (_logger.isDebugEnabled()) {
				_logger.debug("The following is the command line sent to the pwrapper:");
				for (String c : command)
					_logger.debug(c);
			}

			ProcessWrapper wrapper = ProcessWrapperFactory.createWrapper(_commonDirectory);

			if (_environment == null)
				_environment = new HashMap<String, String>();

			setExportedEnvironment(_environment);
			history.createDebugWriter("Activity Environment Set").format("Activity environment set to %s", _environment).close();

			if (_environment != null) {
				String ogrshConfig = _environment.get("OGRSH_CONFIG");
				if (ogrshConfig != null) {
					File f = new File(workingDirectory, ogrshConfig);
					_environment.put("OGRSH_CONFIG", f.getAbsolutePath());
				}

				String geniiUserDir = _environment.get("GENII_USER_DIR");
				if (geniiUserDir != null && !geniiUserDir.startsWith("/")) {
					File f = new File(workingDirectory, geniiUserDir);
					_environment.put("GENII_USER_DIR", f.getAbsolutePath());
					_logger.info("rewrote GENII_USER_DIR to be: " + _environment.get("GENII_USER_DIR"));
				}

				_environment = overloadEnvironment(_environment);
			}

			stderrFile = _redirects.stderrSink(workingDirectory);

			// assemble job properties for cmdline manipulators
			Collection<String> args = new ArrayList<String>(command.size()-1);
			for (int i = 1; i < command.size(); i++)
				args.add(command.get(i));

			// old code
			// File resourceUsageFile = new ResourceUsageDirectory(workingDirectory).getNewResourceUsageFile();
			// 2020-04-18 by ASG during coronovirus to put the accounting stuff in place
			// This next call establishes both the job working directory, JWD, and the Accounting directory for the file.
			//resourceUsageFile = new ResourceUsageDirectory(_workingDirectory.getWorkingDirectory()).getNewResourceUsageFile();
			ResourceUsageDirectory tmp=new ResourceUsageDirectory(workingDirectory);
			File resourceUsageFile =tmp.getNewResourceUsageFile();  // This should point to the accounting directory, not create a properties file.
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
			generateProperties(tmp,userName,_executable.getAbsolutePath(), _memory, _numProcesses,
					_numProcessesPerHost, _threadsPerProcess, activity);
			generateJSDL(tmp, activity);

			// End of updates 2020-04-18

			HashMap<String, Object> jobProperties = new HashMap<String, Object>();
			CmdLineManipulatorUtils.addBasicJobProperties(jobProperties, _executable.getAbsolutePath(), args);
			CmdLineManipulatorUtils.addEnvProperties(jobProperties, _fuseMountPoint, _environment, workingDirectory, _redirects.stdinSource(),
				_redirects.stdoutSink(), stderrFile, resourceUsageFile, wrapper.getPathToWrapper());
			CmdLineManipulatorUtils.addSPMDJobProperties(jobProperties, _spmdVariation, _numProcesses, _numProcessesPerHost,
				_threadsPerProcess);

			newCmdLine = new Vector<String>();
			if (_logger.isDebugEnabled())
				_logger.debug("Trying to call cmdLine manipulators.");
			newCmdLine =
				CmdLineManipulatorUtils.callCmdLineManipulators(jobProperties, _constructionParameters.getCmdLineManipulatorConfiguration());

			// for testing only - use default cmdLine format to compare to transform
			String[] arguments = new String[command.size() - 1];
			for (int lcv = 1; lcv < command.size(); lcv++)
				arguments[lcv - 1] = command.get(lcv);

			Vector<String> testCmdLine = wrapper.formCommandLine(_fuseMountPoint, _environment, workingDirectory, _redirects.stdinSource(),
				_redirects.stdoutSink(), stderrFile, resourceUsageFile, command.get(0), arguments);
			if (_logger.isDebugEnabled())
				_logger.debug(String.format("Previous cmdLine format with pwrapper only:\n %s", testCmdLine.toString()));

			if (_logger.isDebugEnabled())
				_logger.debug("Trying to start a new process on machine using fork/exec or spawn.");
			preDelay();

			PrintWriter hWriter = history.createInfoWriter("BES Starting Activity").format("BES starting activity: ");
			for (String arg : newCmdLine)
				hWriter.format(" %s", arg);
			hWriter.close();
			_logger.info(String.format("Executing job for userID '%s' using command line:\n\t%s", userName, testCmdLine.toString()));

			token = wrapper.execute(_fuseMountPoint, _environment, workingDirectory, _redirects.stdinSource(), resourceUsageFile, newCmdLine);
			// ASG - TEST code remove if found
			_process=token;
			// End test
		}

		try {
			token.join();
			postDelay();
			ExitResults results = token.results();

			if (_hardTerminate != null && _hardTerminate.booleanValue()) {
				if (_countAsFailedAttempt) {
					String msg = "The process was forceably killed";
					history.warn(msg);
					_logger.warn(msg);
					throw new ContinuableExecutionException(msg);
				} else {
					if (_logger.isDebugEnabled())
						_logger.debug("hard terminate was true, but this is not to count as failed attempt.  ignoring.");
				}

				return;
			}
			// ASG 2019-01-13 At some point we will need to do accounting for jobs that disappeared. 
			if (results == null) {
				_logger.error("Somehow we got an exit with no exit results.");
				history.debug("Job Exited with No Resulst");
			} else {
				history.trace("Job Exited with Exit Code %d", results.exitCode());

				_logger.info(String.format("Process exited with exit-code %d.", results.exitCode()));

				AccountingService acctService = ContainerServices.findService(AccountingService.class);
				if (acctService != null) {
					acctService.addAccountingRecord(context.getCallingContext(), context.getBESEPI(), null, null, null, newCmdLine,
						results.exitCode(), results.userTime(), results.kernelTime(), results.wallclockTime(), results.maximumRSS(),1);
				}
			}

			appendStandardError(history, stderrFile);
		} catch (InterruptedException ie) {
			synchronized (_processLock) {
				history.warn("Process Interrupted - Killing");
				destroyProcess(_process);
				_process = null;
			}
		} finally {
			synchronized (_processLock) {
				_process = null;
			}
		}
	}

	@Override
	public void terminate(boolean countAsFailedAttempt) throws ExecutionException
	{
		HistoryContext history = HistoryContextFactory.createContext(HistoryEventCategory.Terminating);

		history.info("Terminating Activity Per Request");

		synchronized (_processLock) {
			_hardTerminate = true;
			_countAsFailedAttempt = countAsFailedAttempt;
			destroyProcess(_process);
		}
	}
}
