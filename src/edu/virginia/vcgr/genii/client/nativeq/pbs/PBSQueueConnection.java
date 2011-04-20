package edu.virginia.vcgr.genii.client.nativeq.pbs;

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

import edu.virginia.vcgr.genii.client.bes.ResourceOverrides;
import edu.virginia.vcgr.genii.client.nativeq.ApplicationDescription;
import edu.virginia.vcgr.genii.client.nativeq.BulkStatusFetcher;
import edu.virginia.vcgr.genii.client.nativeq.JobStateCache;
import edu.virginia.vcgr.genii.client.nativeq.JobToken;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueueConfiguration;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueueException;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueueState;
import edu.virginia.vcgr.genii.client.nativeq.ScriptBasedQueueConnection;
import edu.virginia.vcgr.genii.client.nativeq.ScriptLineParser;
import edu.virginia.vcgr.genii.cmdLineManipulator.config.CmdLineManipulatorConfiguration;
import edu.virginia.vcgr.genii.container.bes.jsdl.personality.common.ResourceConstraints;

public class PBSQueueConnection extends ScriptBasedQueueConnection<PBSQueueConfiguration>
{
	static private Log _logger = LogFactory.getLog(PBSQueueConnection.class);
	
	static final public long DEFAULT_CACHE_WINDOW = 1000L * 30;
	static final public URI PBS_MANAGER_TYPE = URI.create(
		"http://vcgr.cs.virginia.edu/genesisII/nativeq/pbs");
	
	static private String toWallTimeFormat(double value)
	{
		long total = (long)value;
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
	
	PBSQueueConnection(ResourceOverrides resourceOverrides,
		CmdLineManipulatorConfiguration cmdLineManipulatorConf,
		File workingDirectory,
		NativeQueueConfiguration nativeQueueConfig,
		PBSQueueConfiguration pbsConfig, String queueName,
		List<String> qsubStart, List<String> qstatStart, List<String> qdelStart,
		JobStateCache statusCache)
			throws NativeQueueException
	{
		super(workingDirectory, resourceOverrides, 
				cmdLineManipulatorConf, nativeQueueConfig, pbsConfig);
		
		_statusCache = statusCache;
		
		int index = queueName.indexOf('@');
		if (index >= 0)
		{
			_qName = queueName.substring(0, index);
			_destination = queueName.substring(index + 1);
		} else
		{
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
		
		String arg = token.toString();
		if (_destination != null)
			arg = arg + "@" + _destination;
		commandLine.add(arg);
		ProcessBuilder builder = new ProcessBuilder(commandLine);
		execute(builder);
	}

	static private class JobStatusParser implements ScriptLineParser
	{
		static private Pattern JOB_TOKEN_PATTERN =
			Pattern.compile("^\\s*Job Id:\\s*(\\S+)\\s*$");
		static private Pattern JOB_STATE_PATTERN =
			Pattern.compile("^\\s*job_state\\s*=\\s*(\\S+)\\s*$");
		
		private Map<String, String> _matchedPairs =
			new HashMap<String, String>();
		
		private String _lastToken = null;
		private String _lastState = null;
		
		@Override
		public Pattern[] getHandledPatterns()
		{
			return new Pattern[] { JOB_TOKEN_PATTERN, JOB_STATE_PATTERN };
		}

		@Override
		public void parseLine(Matcher matcher) throws NativeQueueException
		{
			if (matcher.pattern() == JOB_TOKEN_PATTERN)
				_lastToken = matcher.group(1);
			else if (matcher.pattern() == JOB_STATE_PATTERN)
			{
				_lastState = matcher.group(1);
				if (_lastToken == null)
					throw new NativeQueueException(
						"Unable to parse status output.");
				
				_matchedPairs.put(_lastToken, _lastState);
				_lastToken = null;
				_lastState = null;
			} else
				throw new NativeQueueException(
					"Unable to parse status output.");
		}
		
		public Map<String, String> getStateMap()
			throws NativeQueueException
		{
			return _matchedPairs;
		}
	}
	
	private class BulkPBSStatusFetcher implements BulkStatusFetcher
	{
		@Override
		public Map<JobToken, NativeQueueState> getStateMap()
				throws NativeQueueException
		{
			Map<JobToken, NativeQueueState> ret = 
				new HashMap<JobToken, NativeQueueState>();
			
			List<String> commandLine = new LinkedList<String>();
			commandLine.addAll(_qstatStart);
			commandLine.add("-f");
			
			if (_destination != null)
				commandLine.add(String.format("@%s", _destination));
			
			ProcessBuilder builder = new ProcessBuilder(commandLine);
			String result = execute(builder);
			JobStatusParser parser = new JobStatusParser();
			parseResult(result, parser);
			Map<String, String> stateMap = parser.getStateMap();
			for (String tokenString : stateMap.keySet())
			{
				PBSJobToken token = new PBSJobToken(tokenString);
				PBSQueueState state = PBSQueueState.fromStateSymbol(
					stateMap.get(tokenString));
				_logger.debug(String.format("Putting %s[%s]\n", token, state));
				ret.put(token, state);
			}
			
			return ret;
		}
	}
	
	@Override
	public NativeQueueState getStatus(JobToken token)
			throws NativeQueueException
	{
		NativeQueueState state = _statusCache.get(
			token, new BulkPBSStatusFetcher(), DEFAULT_CACHE_WINDOW);
		if (state == null)
			state = PBSQueueState.fromStateSymbol("E");
		
		return state;
	}

	@Override
	protected void generateQueueHeaders(PrintStream script,
			File workingDirectory, ApplicationDescription application)
			throws NativeQueueException, IOException
	{
		super.generateQueueHeaders(script, workingDirectory, application);
		
		if (application.getSPMDVariation() != null)
		{
			//add directives for specifying stdout and stderr redirects
			script.format("#PBS -o %s\n", application.getStdoutRedirect(workingDirectory));
			script.format("#PBS -e %s\n", application.getStderrRedirect(workingDirectory));
			
			//add directive for specifying multiple processors
			Integer numProcs = application.getNumProcesses();
			Integer numProcsPerHost = application.getNumProcessesPerHost();
						
			if (numProcs != null) 
			{
				if (numProcsPerHost != null)
				{
					Integer hosts = numProcs / numProcsPerHost;
					script.format("#PBS -l nodes=%d:ppn=%d\n", hosts.intValue(), numProcsPerHost.intValue());
				}
				else 
				{
					script.format("#PBS -l nodes=%d:ppn=1\n", numProcs.intValue());	
				}
			}
			
		}
		
		ResourceConstraints resourceConstraints = 
			application.getResourceConstraints();
		if (resourceConstraints != null)
		{
			Double totalPhyscialMemory = 
				resourceConstraints.getTotalPhysicalMemory();
			if ( (totalPhyscialMemory != null) &&
				(!totalPhyscialMemory.equals(Double.NaN)) )
				script.format("#PBS -l mem=%d\n", totalPhyscialMemory.longValue());
			
			Double wallclockTime = resourceConstraints.getWallclockTimeLimit();
			if (wallclockTime != null && !wallclockTime.equals(Double.NaN))
				script.format("#PBS -l walltime=%s\n", toWallTimeFormat(wallclockTime));
		}
	}
	
	@Override
	protected List<String> generateApplicationBody(PrintStream script,
			File workingDirectory, ApplicationDescription application)
			throws NativeQueueException, IOException
	{
		URI variation = application.getSPMDVariation();
		if (variation != null){
			//temporarily set std redirects to null; these are written as pbs directives
			File stdoutRedirect = application.getStdoutRedirect(workingDirectory);
			File stderrRedirect = application.getStderrRedirect(workingDirectory);
			
			application.setStdoutRedirect(null);
			application.setStderrRedirect(null);
			
			//proceed as usual
			List<String> finalCmdLine = 
				super.generateApplicationBody(script, workingDirectory, application);
			
			//reset std redirects in application description
			if (stdoutRedirect != null)
				application.setStdoutRedirect(stdoutRedirect.toString());
			if (stderrRedirect != null)
				application.setStderrRedirect(stderrRedirect.toString());
						
			return finalCmdLine;
		} else
			return super.generateApplicationBody(script, workingDirectory, application);
	}

	@Override
	public JobToken submit(ApplicationDescription application) 
		throws NativeQueueException
	{
		Pair<File, List<String>> submissionReturn = 
			generateSubmitScript(getWorkingDirectory(), application);
		
		List<String> command = new LinkedList<String>();
		
		command.addAll(_qsubStart);
		if (_qName != null || _destination != null)
		{
			command.add("-q");
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
		
		return new PBSJobToken(
				execute(builder).trim(), submissionReturn.second());
	}

	@Override
	public int getExitCode(JobToken token) throws NativeQueueException
	{
		BufferedReader reader = null;
		
		try
		{
			reader = new BufferedReader(new FileReader(
				new File(getWorkingDirectory(), QUEUE_SCRIPT_RESULT_FILENAME)));
			String line = reader.readLine();
			if (line == null)
				throw new NativeQueueException(
					"Unable to determine application exit status.");
			return Integer.parseInt(line.trim());
		}
		catch (FileNotFoundException ioe)
		{
			throw new NativeQueueException(
				"Application doesn't appear to have exited.", ioe);
		}
		catch (IOException ioe)
		{
			throw new NativeQueueException(
				"Unable to determine application exit status.", ioe);
		}
	}
}
