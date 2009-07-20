package edu.virginia.vcgr.genii.client.nativeq.pbs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.util.ArrayList;
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
import edu.virginia.vcgr.genii.client.nativeq.ApplicationDescription;
import edu.virginia.vcgr.genii.client.nativeq.BasicResourceAttributes;
import edu.virginia.vcgr.genii.client.nativeq.BulkStatusFetcher;
import edu.virginia.vcgr.genii.client.nativeq.FactoryResourceAttributes;
import edu.virginia.vcgr.genii.client.nativeq.JobStateCache;
import edu.virginia.vcgr.genii.client.nativeq.JobToken;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueueException;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueueState;
import edu.virginia.vcgr.genii.client.nativeq.ScriptBasedQueueConnection;
import edu.virginia.vcgr.genii.client.nativeq.ScriptLineParser;
import edu.virginia.vcgr.genii.client.spmd.SPMDException;
import edu.virginia.vcgr.genii.client.spmd.SPMDTranslator;
import edu.virginia.vcgr.genii.client.spmd.SPMDTranslatorFactories;
import edu.virginia.vcgr.genii.client.spmd.SPMDTranslatorFactory;

public class PBSQueueConnection extends ScriptBasedQueueConnection
{
	static private Log _logger = LogFactory.getLog(PBSQueueConnection.class);
	
	static final public long DEFAULT_CACHE_WINDOW = 1000L * 30;
	static final public URI PBS_MANAGER_TYPE = URI.create(
		"http://vcgr.cs.virginia.edu/genesisII/nativeq/pbs");
	
	private JobStateCache _statusCache;
	
	private String _qName;
	
	private File _qsubBinary;
	private File _qstatBinary;
	private File _qdelBinary;
	
	PBSQueueConnection(File workingDirectory, Properties connectionProperties,
		String queueName, File qsubBinary, File qstatBinary, File qdelBinary,
		JobStateCache statusCache)
			throws NativeQueueException
	{
		super(workingDirectory, connectionProperties);
		
		_statusCache = statusCache;
		
		_qName = queueName;
		
		_qsubBinary = qsubBinary;
		_qstatBinary = qstatBinary;
		_qdelBinary = qdelBinary;
		
		checkBinary(_qsubBinary);
		checkBinary(_qsubBinary);
		checkBinary(_qsubBinary);
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
		ProcessBuilder builder = new ProcessBuilder(
			_qdelBinary.getAbsolutePath(), token.toString());
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
			
			ProcessBuilder builder = new ProcessBuilder(
				_qstatBinary.getAbsolutePath(), "-f");
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
					script.format("#PBS -l nodes=%d:ppn=%d\n", numProcs.intValue(), numProcsPerHost.intValue());
				}
				else 
				{
					script.format("#PBS -l nodes=%d:ppn=1\n", numProcs.intValue());	
				}
			}
		}
	}

	@Override
	protected void generateApplicationBody(PrintStream script,
			File workingDirectory, ApplicationDescription application)
			throws NativeQueueException, IOException
	{
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
				commandLine = translator.translateCommandLine(commandLine);
				boolean first = true;
				for (String val : commandLine)
				{
					script.format("%s\"%s\"", (first ? "" : " "), val);
					first = false;
				}
			}
			catch (SPMDException se)
			{
				throw new NativeQueueException(
					"Unable to translate SPMD command.", se);
			}
			
			if (application.getStdinRedirect() != null)
				script.format(" < \"%s\"", application.getStdinRedirect());
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
		if (_qName != null)
		{
			command.add("-q");
			command.add(_qName);
		}
		
		Map<String, String> environment = application.getEnvironment();
		if (environment.size() > 0)
		{
			command.add("-v");
			StringBuilder builder = new StringBuilder();
			boolean first = true;
			for (String key : environment.keySet())
			{
				if (!first)
					builder.append(",");
				first = false;
				builder.append(String.format(
					"%s=%s", key, environment.get(key)));
			}
			command.add(builder.toString());
		}
		
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
		}catch (IOException ioe)
		{
			throw new NativeQueueException(
				"Unable to determine application exit status.", ioe);
		}
	}
}
