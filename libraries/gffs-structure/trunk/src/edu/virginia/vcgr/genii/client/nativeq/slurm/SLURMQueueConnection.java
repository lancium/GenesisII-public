package edu.virginia.vcgr.genii.client.nativeq.slurm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.Pair;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.bes.ResourceOverrides;
import edu.virginia.vcgr.genii.client.nativeq.ApplicationDescription;
import edu.virginia.vcgr.genii.client.nativeq.BulkStatusFetcher;
import edu.virginia.vcgr.genii.client.nativeq.JobStateCache;
import edu.virginia.vcgr.genii.client.nativeq.JobToken;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueueConfiguration;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueueException;
import edu.virginia.vcgr.genii.client.nativeq.QueueResultsException;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueueState;
import edu.virginia.vcgr.genii.client.nativeq.ScriptBasedQueueConnection;
import edu.virginia.vcgr.genii.client.nativeq.ScriptLineParser;
import edu.virginia.vcgr.genii.cmdLineManipulator.CmdLineManipulatorConstants;
import edu.virginia.vcgr.genii.cmdLineManipulator.config.CmdLineManipulatorConfiguration;
import edu.virginia.vcgr.genii.client.jsdl.personality.common.ResourceConstraints;

public class SLURMQueueConnection extends ScriptBasedQueueConnection<SLURMQueueConfiguration>
{
	static private Log _logger = LogFactory.getLog(SLURMQueueConnection.class);

	static final public long DEFAULT_CACHE_WINDOW = 1000L * 30;
	static final public URI SLURM_MANAGER_TYPE = URI.create("http://vcgr.cs.virginia.edu/genesisII/nativeq/slurm");

	static private String toWallTimeFormat(double value)
	{
		long total = (long) value;
		long seconds = total % 60;
		total /= 60;
		long minutes = total % 60;
		total /= 60;
		long hours = total;

		if (hours > 0)
			return String.format("%d:%d:%d", hours, minutes, seconds);
		else if (minutes > 0)
			return String.format("%d:%d", minutes, seconds);
		else
			return String.format("%d", seconds);
	}

	private JobStateCache _statusCache;

	private String _qName;
	private String _destination = null;

	private List<String> _qsubStart;
	private List<String> _qstatStart;
	private List<String> _qdelStart;

	SLURMQueueConnection(ResourceOverrides resourceOverrides, CmdLineManipulatorConfiguration cmdLineManipulatorConf, File workingDirectory,
		NativeQueueConfiguration nativeQueueConfig, SLURMQueueConfiguration slurmConfig, String queueName, List<String> qsubStart,
		List<String> qstatStart, List<String> qdelStart, JobStateCache statusCache) throws NativeQueueException
	{
		super(workingDirectory, resourceOverrides, cmdLineManipulatorConf, nativeQueueConfig, slurmConfig);

		_statusCache = statusCache;

		int index = queueName.indexOf('@');
		if (index >= 0) {
			_qName = queueName.substring(0, index);
			_destination = queueName.substring(index + 1);
		} else {
			_qName = queueName;
			_destination = null;
		}

		_qsubStart = qsubStart;
		_qstatStart = qstatStart;
		_qdelStart = qdelStart;
	}

	@Override
	public void cancel(JobToken token) throws NativeQueueException
	{
		List<String> commandLine = new LinkedList<String>();
		commandLine.addAll(_qdelStart);

		// make sure they pay attention to us.
		commandLine.add("--signal=KILL");

		String arg = token.toString();
		if (_destination != null)
			arg = arg + "@" + _destination;
		commandLine.add(arg);
		ProcessBuilder builder = new ProcessBuilder(commandLine);

		if (_logger.isDebugEnabled())
			_logger.debug("attempting to cancel slurm job: " + token);

		execute(builder);
	}

	static private class JobStatusParser implements ScriptLineParser
	{
		static private Pattern JOB_TOKEN_AND_STATE_PATTERN =
			Pattern.compile("^\\s*id\\s*=\\s*(\\S+)\\s+state\\s*=\\s*(\\S+)\\s+user\\s*=\\s*(\\S+)\\s*$");

		private Map<String, String> _matchedPairs = new HashMap<String, String>();

		private String _lastJobID = null;
		private String _lastJobState = null;

		@Override
		public Pattern[] getHandledPatterns()
		{
			return new Pattern[] { JOB_TOKEN_AND_STATE_PATTERN };
		}

		@Override
		public void parseLine(Matcher matcher) throws NativeQueueException
		{
			_lastJobID = matcher.group(1);
			_lastJobState = matcher.group(2);
			String user = matcher.group(3);

			// future: is it useful to know the user? we do know it for slurm.

			if ((_lastJobID == null) || (_lastJobState == null)) {
				String msg = "Unable to parse status output.";
				_logger.error(msg);
				throw new NativeQueueException(msg);
			}
			_matchedPairs.put(_lastJobID, _lastJobState);

			if (_logger.isTraceEnabled()) {
				_logger.debug("added a token: " + _lastJobID + ", " + _lastJobState + " (with unused user name of " + user + ")");
			}
			_lastJobID = null;
			_lastJobState = null;
		}

		public Map<String, String> getStateMap() throws NativeQueueException
		{
			return _matchedPairs;
		}
	}

	private class BulkSLURMStatusFetcher implements BulkStatusFetcher
	{
		@Override
		public Map<JobToken, NativeQueueState> getStateMap() throws NativeQueueException
		{
			Map<JobToken, NativeQueueState> ret = new HashMap<JobToken, NativeQueueState>();

			// command line is configured to make squeue spit out the id, state and owner on one line.
			List<String> commandLine = new LinkedList<String>();
			commandLine.addAll(_qstatStart);
			commandLine.add("-h");
			// Removed 2019-07-16 by ASG. Version 19 of SLURM barfs if it gets -l and -o
			// commandLine.add("-l");
			commandLine.add("-o");
			commandLine.add("id=%A state=%t user=%u");

			if (_destination != null)
				commandLine.add(String.format("@%s", _destination));

			ProcessBuilder builder = new ProcessBuilder(commandLine);

			String result = execute(builder);
			JobStatusParser parser = new JobStatusParser();
			parseResult(result, parser);
			Map<String, String> stateMap = parser.getStateMap();

			for (String tokenString : stateMap.keySet()) {
				SLURMJobToken token = new SLURMJobToken(tokenString);
				SLURMQueueState state = SLURMQueueState.fromStateSymbol(stateMap.get(tokenString));
				if (_logger.isTraceEnabled())
					_logger.debug(String.format("slurm status: token %s state %s\n", token, state));
				ret.put(token, state);
			}

			return ret;
		}
	}

	@Override
	public NativeQueueState getStatus(JobToken token) throws NativeQueueException
	{
		NativeQueueState state = _statusCache.get(token, new BulkSLURMStatusFetcher(), DEFAULT_CACHE_WINDOW);
		if (_logger.isTraceEnabled())
			_logger.debug("received a parsed queue state of: " + state);
		if (state == null) {
			state = SLURMQueueState.fromStateSymbol("CD");
			_logger.warn("job state received was missing (probably exited), so setting state to: " + state);
		}
		return state;
	}

	@Override
	protected void generateQueueHeaders(PrintStream script, File workingDirectory, ApplicationDescription application)
		throws NativeQueueException, IOException
	{
		super.generateQueueHeaders(script, workingDirectory, application);

		// add directives for specifying stdout and stderr redirects
		script.format("#SBATCH -o %s\n", application.getStdoutRedirect(workingDirectory));
		script.format("#SBATCH -e %s\n", application.getStderrRedirect(workingDirectory));

		if (application.getSPMDVariation() != null) {
			// add directive for specifying multiple processors
			Integer numProcs = application.getNumProcesses();
			Integer numProcsPerHost = application.getNumProcessesPerHost();
			Integer threadsPerProcess = application.getThreadsPerProcess();

			if (_logger.isDebugEnabled())
				_logger.debug(
					"slurm spmd info: numProcs=" + numProcs + " numProcsPerHost=" + numProcsPerHost + " threadsPerProc=" + threadsPerProcess);

			// new section for checking whether they've asked for exclusivity or are okay with sharing the node for sequential jobs.
			if (application.getSPMDVariation().toString().contains(CmdLineManipulatorConstants.NODE_EXCLUSIVE_THREADED_PHRASE)) {
				script.format("#SBATCH --exclusive\n");
				if (_logger.isDebugEnabled())
					_logger.debug("slurm using exclusive flag for NodeExclusiveThreaded spmd");
			} else if (application.getSPMDVariation().toString().contains(CmdLineManipulatorConstants.SHARED_THREADED_PHRASE)) {
				script.format("#SBATCH --share\n");
				if (_logger.isDebugEnabled())
					_logger.debug("slurm using shared flag for SharedThreaded spmd");
			}

			// in slurm, processes are tasks. so we only worry about tasks/processes and processes per host here.
			if (numProcs != null) {
				// always specify number of tasks if they told us.
				script.format("#SBATCH --ntasks=%d\n", numProcs.intValue());
				// if we also know processes per host, add in the node and tasks per node counts.
				if (numProcsPerHost != null) {
					Integer hosts = numProcs / numProcsPerHost;
					script.format("#SBATCH --nodes=%d\n", hosts.intValue());
					script.format("#SBATCH --ntasks-per-node=%d\n", numProcsPerHost.intValue());
				}
			}

			// if they provided the number of threads per process, then we also specify that. this is separate from task counting.
			if (threadsPerProcess != null) {
				script.format("#SBATCH --cpus-per-task=%d\n", threadsPerProcess.intValue());
			}

		}

		ResourceConstraints resourceConstraints = application.getResourceConstraints();
		if (resourceConstraints != null) {
			Double totalPhysicalMemory = resourceConstraints.getTotalPhysicalMemory();
			if ((totalPhysicalMemory != null) && (!totalPhysicalMemory.equals(Double.NaN)))
				script.format("#SBATCH --mem-per-cpu=%d\n", (totalPhysicalMemory.longValue()/(1024*1024)));
			// ASG 2019-03-19 SLURM expects memory in MB not bytes, causes failures.

			Double wallclockTime = resourceConstraints.getWallclockTimeLimit();
			if (wallclockTime != null && !wallclockTime.equals(Double.NaN))
				script.format("#SBATCH --time=%s\n", toWallTimeFormat(wallclockTime));
		}
	}

	@Override
	protected List<String> generateApplicationBody(PrintStream script, File workingDirectory, ApplicationDescription application)
		throws NativeQueueException, IOException
	{
		/*
		 * VANA commented the whole section URI variation = application.getSPMDVariation(); if (variation != null) { // temporarily set std
		 * redirects to null; these are written as slurm directives File stdoutRedirect = application.getStdoutRedirect(workingDirectory);
		 * File stderrRedirect = application.getStderrRedirect(workingDirectory);
		 * 
		 * application.setStdoutRedirect(null); application.setStderrRedirect(null);
		 * 
		 * // proceed as usual List<String> finalCmdLine = super.generateApplicationBody(script, workingDirectory, application);
		 * 
		 * // reset std redirects in application description if (stdoutRedirect != null)
		 * application.setStdoutRedirect(stdoutRedirect.toString()); if (stderrRedirect != null)
		 * application.setStderrRedirect(stderrRedirect.toString());
		 * 
		 * return finalCmdLine; } else
		 */
		return super.generateApplicationBody(script, workingDirectory, application);
	}

	@Override
	public JobToken submit(ApplicationDescription application) throws NativeQueueException
	{
		Pair<File, List<String>> submissionReturn = generateSubmitScript(getWorkingDirectory(), application);

		List<String> command = new LinkedList<String>();

		command.addAll(_qsubStart);
		if (_qName != null || _destination != null) {
			command.add("-p");
			StringBuilder builder = new StringBuilder();
			if (_qName != null)
				builder.append(_qName);

			if (_destination != null)
				builder.append(String.format("@%s", _destination));

			command.add(builder.toString());
		}

		command.add(submissionReturn.first().getAbsolutePath());

		ProcessBuilder builder = new ProcessBuilder(command);
		builder.directory(getWorkingDirectory());

		return new SLURMJobToken(execute(builder).trim(), submissionReturn.second());
	}

	@Override
	public int getExitCode(JobToken token) throws NativeQueueException, QueueResultsException, FileNotFoundException, IOException
	{
		BufferedReader reader = null;

		try {
			reader = new BufferedReader(new FileReader(new File(getWorkingDirectory(), QUEUE_SCRIPT_RESULT_FILENAME)));
			String line = reader.readLine();
			if (line == null) {
				StreamUtils.close(reader);
				throw new QueueResultsException("Unable to determine application exit status.");
			}
			StreamUtils.close(reader);
			return Integer.parseInt(line.trim());
		} catch (FileNotFoundException ioe) {
			// Changed to throw a ContinuableExecutionException. Now there are continuable conditions from not finding exit codes,
			// e.g., if the scheduler terminated it, or the nodes died, we still want to stageout.
			throw new QueueResultsException("Pwrapper queue.script.result does not exist.", ioe);
		} catch (IOException ioe) {
			throw new QueueResultsException("Unable to determine application exit status.", ioe);
		}
	}

	public static void main(String[] args)
	{
		JobStatusParser parser = new JobStatusParser();
		String[] lines = { "id=36697 state=CG user=ak3ka", "id=36799 state=R user=ak3ka", "id=36808 state=R user=ak3ka",
			"id=36809 state=R user=ak3ka", "id=36810 state=R user=ak3ka", "id=36901 state=PD user=cak0l", "id=36902 state=PD user=cak0l",
			"id=36903 state=PD user=cak0l" };

		for (String line : lines) {
			for (Pattern pattern : parser.getHandledPatterns()) {
				Matcher matcher = pattern.matcher(line);
				if (matcher.matches()) {
					try {
						parser.parseLine(matcher);
						_logger.info("success parsing, got: id=" + parser._lastJobID + " state=" + parser._lastJobState);
					} catch (NativeQueueException e) {
						_logger.error("failed to parse line with exception", e);
					}
					break;
				} else {
					_logger.debug("FAILED TO MATCH THE PATTERN, PATTERN IS WRONG");
				}
			}
		}

	}
}
