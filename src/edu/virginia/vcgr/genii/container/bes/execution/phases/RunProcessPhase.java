package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.net.URI;
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
		super(new ActivityState(ActivityStateEnumeration.Running, EXECUTING_STAGE, false), constructionParameters);

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

		if (executable.endsWith(".simg") || executable.endsWith(".sif") || executable.endsWith(".qcow2")) {
			if (_logger.isDebugEnabled())
				_logger.debug("Handling image executable (.simg/.sif or .qcow2)...");
			if (executable.endsWith(".qcow2")) {
				_executable = new File("../vmwrapper.sh");
			}
			else {
				_executable = new File("../singularity-wrapper.sh");
			}
			if (_logger.isDebugEnabled())
				_logger.debug("Using image executable: " + _executable.getAbsolutepath());

			// If using wrapper executable, prepend original executable to arguments list
			_arguments = new String[arguments.length()+1];
			_arguments[0] = executable.getAbsolutePath();
			for (int i = 0; i < arguments.length(); i++) {
				_arguments[1 + i] = arguments[i];
			}
		}
		else {
			_executable = executable;
			_arguments = arguments;
		}
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
	public void execute(ExecutionContext context) throws Throwable
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
			command = new Vector<String>();
			command.add(_executable.getAbsolutePath());
			for (String arg : _arguments)
				command.add(arg);

			File workingDirectory = context.getCurrentWorkingDirectory().getWorkingDirectory();

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
			Collection<String> args = new ArrayList<String>(_arguments.length);
			for (String s : _arguments)
				args.add(s);

			// old code
			// File resourceUsageFile = new ResourceUsageDirectory(workingDirectory).getNewResourceUsageFile();
			// 2020-04-18 by ASG during coronovirus to put the accounting stuff in place
			// This next call establishes both the job working directory, JWD, and the Accounting directory for the file.
			//resourceUsageFile = new ResourceUsageDirectory(_workingDirectory.getWorkingDirectory()).getNewResourceUsageFile();
			ResourceUsageDirectory tmp=new ResourceUsageDirectory(workingDirectory);
			File resourceUsageFile =tmp.getNewResourceUsageFile();  // This should point to the accounting directory, not create a properties file.
			generateProperties(tmp,userName,_executable.getAbsolutePath(), _memory, _numProcesses,
					_numProcessesPerHost, _threadsPerProcess );

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
			_hardTerminate = Boolean.TRUE;
			_countAsFailedAttempt = countAsFailedAttempt;
			destroyProcess(_process);
		}
	}
}
