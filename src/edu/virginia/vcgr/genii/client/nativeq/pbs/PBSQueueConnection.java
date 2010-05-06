package edu.virginia.vcgr.genii.client.nativeq.pbs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.jsdl.JSDLUtils;
import edu.virginia.vcgr.genii.client.nativeq.AdditionalArguments;
import edu.virginia.vcgr.genii.client.nativeq.ApplicationDescription;
import edu.virginia.vcgr.genii.client.nativeq.BasicResourceAttributes;
import edu.virginia.vcgr.genii.client.nativeq.BulkStatusFetcher;
import edu.virginia.vcgr.genii.client.nativeq.FactoryResourceAttributes;
import edu.virginia.vcgr.genii.client.nativeq.JobStateCache;
import edu.virginia.vcgr.genii.client.nativeq.JobToken;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueue;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueueException;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueueState;
import edu.virginia.vcgr.genii.client.nativeq.ScriptBasedQueueConnection;
import edu.virginia.vcgr.genii.client.nativeq.ScriptLineParser;
import edu.virginia.vcgr.genii.client.nativeq.UnixSignals;
import edu.virginia.vcgr.genii.client.pwrapper.ProcessWrapper;
import edu.virginia.vcgr.genii.client.pwrapper.ProcessWrapperException;
import edu.virginia.vcgr.genii.client.pwrapper.ProcessWrapperFactory;
import edu.virginia.vcgr.genii.client.spmd.SPMDException;
import edu.virginia.vcgr.genii.client.spmd.SPMDTranslator;
import edu.virginia.vcgr.genii.client.spmd.SPMDTranslatorFactories;
import edu.virginia.vcgr.genii.client.spmd.SPMDTranslatorFactory;
import edu.virginia.vcgr.genii.container.bes.jsdl.personality.common.ResourceConstraints;

public class PBSQueueConnection extends ScriptBasedQueueConnection
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
	
	private File _qsubBinary;
	private File _qstatBinary;
	private File _qdelBinary;
	
	private AdditionalArguments _additionalArguments;
	
	PBSQueueConnection(File workingDirectory, Properties connectionProperties,
		String queueName, File qsubBinary, File qstatBinary, File qdelBinary,
		AdditionalArguments additionalArguments, JobStateCache statusCache)
			throws NativeQueueException
	{
		super(workingDirectory, connectionProperties);
		
		_statusCache = statusCache;
		
		_additionalArguments = additionalArguments;
		
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
		
		_qsubBinary = qsubBinary;
		_qstatBinary = qstatBinary;
		_qdelBinary = qdelBinary;
		
		checkBinary(_qsubBinary);
		checkBinary(_qstatBinary);
		checkBinary(_qdelBinary);
	}
	
	@Override
	protected void addSupportedSPMDVariations(
		Map<URI, SPMDTranslator> variations, Properties connectionProperties)
			throws NativeQueueException
	{
		super.addSupportedSPMDVariations(variations, connectionProperties);
		
		try
		{
			for (int lcv = 0; true; lcv++)
			{
				String variationProperty =
					PBSQueue.QUEUE_SUPPORTED_SPMD_VARIATIONS_PROPERTY_BASE +
					"." + lcv;
				
				String variation = connectionProperties.getProperty(
					variationProperty);
				
				if (variation == null)
					break;
				
				String providerName = connectionProperties.getProperty(
					variationProperty + "." + 
					PBSQueue.QUEUE_SUPPORTED_SPMD_VARIATION_PROVIDER_FOOTER);
				
				if (providerName == null)
					throw new NativeQueueException(String.format(
						"Native PBS Queue couldn't find SPMD Provider for type \"%s\".",
						variation));
				
				SPMDTranslatorFactory factory = 
					SPMDTranslatorFactories.getSPMDTranslatorFactory(
						providerName);
				
				Properties constructionProps = new Properties();
				String value =
					connectionProperties.getProperty(variationProperty + "." +
						PBSQueue.QUEUE_SUPPORTED_SPMD_ADDITIONAL_CMDLINE_ARGS);
				if (value != null)
					constructionProps.setProperty(
						SPMDTranslatorFactory.ADDITIONAL_CMDLINE_ARGS_PROPERTY,
						value);
				
				variations.put(URI.create(variation), 
					factory.newTranslator(constructionProps));
			}
		}
		catch (SPMDException se)
		{
			throw new NativeQueueException(
				"Unable to instantiate queue provider.", se);
		}
	}

	@Override
	public FactoryResourceAttributes describe() throws NativeQueueException
	{
		return new FactoryResourceAttributes(
			new BasicResourceAttributes(
				JSDLUtils.getLocalOperatingSystem(),
				JSDLUtils.getLocalCPUArchitecture(),
				null, null, null, null),
			null, PBS_MANAGER_TYPE);
	}

	@Override
	public void cancel(JobToken token) throws NativeQueueException
	{
		List<String> commandLine = new LinkedList<String>();
		commandLine.add(_qdelBinary.getAbsolutePath());
		
		for (String additionalArgString : _additionalArguments.qdelArguments())
			commandLine.add(additionalArgString);
		
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
			commandLine.add(_qstatBinary.getAbsolutePath());
			commandLine.add("-f");
			for (String additionalArgString : _additionalArguments.qstatArguments())
				commandLine.add(additionalArgString);
			
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
	protected void generateApplicationBody(PrintStream script,
			File workingDirectory, ApplicationDescription application)
			throws NativeQueueException, IOException
	{
		EnumSet<UnixSignals> signals = UnixSignals.parseTrapAndKillSet(
			connectionProperties().getProperty(
				NativeQueue.SIGNALS_TO_TRAP_AND_KILL));
		
		URI variation = application.getSPMDVariation();
		if (variation != null)
		{
			SPMDTranslator translator = supportedSPMDVariations().get(
				variation);
			if (translator == null)
				throw new NativeQueueException(String.format(
					"Unable to find SPMD translator for variation \"%s\".",
					variation));
			
			script.format("cd \"%s\"\n", workingDirectory.getAbsolutePath());
			
			List<String> commandLine = new ArrayList<String>(16);
			
			String execName = application.getExecutableName();
			if (!execName.contains("/"))
				execName = String.format("./%s", execName);
			
			commandLine.add(execName);
			
			for (String arg : application.getArguments())
				commandLine.add(arg);
			
			try
			{
				ProcessWrapper wrapper = ProcessWrapperFactory.createWrapper(
					getCommonDirectory(),
					getOperatingSystem(), getProcessorArchitecture());
				commandLine = translator.translateCommandLine(commandLine);
				String []args = new String[commandLine.size() - 1];
				for (int lcv = 1; lcv < commandLine.size(); lcv++)
					args[lcv - 1] = commandLine.get(lcv);
				
				boolean first = true;
				for (String element : wrapper.formCommandLine(
					application.getEnvironment(),
					workingDirectory, 
					application.getStdinRedirect(workingDirectory), 
					application.getStdoutRedirect(workingDirectory),
					application.getStderrRedirect(workingDirectory),
					application.getResourceUsagePath(), commandLine.get(0),
					args))
				{
					script.format("%s\"%s\"", (first ? "" : " "), element);
					first = false;
				}
			}
			catch (ProcessWrapperException pwe)
			{
				throw new NativeQueueException(
					"Unable to create command line for SPMD command.", pwe);
			}
			catch (SPMDException se)
			{
				throw new NativeQueueException(
					"Unable to translate SPMD command.", se);
			}
			
			if (signals.size() > 0)
				script.print(" &");
			
			script.println();
		} else
			super.generateApplicationBody(script, workingDirectory, application);
	}

	@Override
	public JobToken submit(ApplicationDescription application) 
		throws NativeQueueException
	{
		File submitScript = generateSubmitScript(getWorkingDirectory(), application);
		
		List<String> command = new LinkedList<String>();
		
		command.add(_qsubBinary.getAbsolutePath());
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
		
		for (String additionalArgString : _additionalArguments.qsubArguments())
			command.add(additionalArgString);
		
		command.add(submitScript.getAbsolutePath());
		
		ProcessBuilder builder = new ProcessBuilder(command);
		builder.directory(getWorkingDirectory());
		return new PBSJobToken(
			execute(builder).trim());
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