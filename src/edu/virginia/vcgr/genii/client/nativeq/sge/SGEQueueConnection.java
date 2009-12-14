package edu.virginia.vcgr.genii.client.nativeq.sge;

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
import edu.virginia.vcgr.genii.client.nativeq.NativeQueueException;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueueState;
import edu.virginia.vcgr.genii.client.nativeq.ScriptBasedQueueConnection;
import edu.virginia.vcgr.genii.client.nativeq.execution.ParsingExecutionEngine;
import edu.virginia.vcgr.genii.client.spmd.SPMDException;
import edu.virginia.vcgr.genii.client.spmd.SPMDTranslator;
import edu.virginia.vcgr.genii.client.spmd.SPMDTranslatorFactories;
import edu.virginia.vcgr.genii.client.spmd.SPMDTranslatorFactory;
import edu.virginia.vcgr.genii.container.bes.jsdl.personality.common.ResourceConstraints;

public class SGEQueueConnection extends ScriptBasedQueueConnection
{
	static private Log _logger = LogFactory.getLog(SGEQueueConnection.class);
	
	static final public long DEFAULT_CACHE_WINDOW = 1000L * 30;
	static final public URI SGE_MANAGER_TYPE = URI.create(
		"http://vcgr.cs.virginia.edu/genesisII/nativeq/sge");
	
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
	
	SGEQueueConnection(File workingDirectory, Properties connectionProperties,
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
					SGEQueue.QUEUE_SUPPORTED_SPMD_VARIATIONS_PROPERTY_BASE +
					"." + lcv;
				
				String variation = connectionProperties.getProperty(
					variationProperty);
				
				if (variation == null)
					break;
				
				String providerName = connectionProperties.getProperty(
					variationProperty + "." + 
					SGEQueue.QUEUE_SUPPORTED_SPMD_VARIATION_PROVIDER_FOOTER);
				
				if (providerName == null)
					throw new NativeQueueException(String.format(
						"Native SGE Queue couldn't find SPMD Provider for type \"%s\".",
						variation));
				
				SPMDTranslatorFactory factory = 
					SPMDTranslatorFactories.getSPMDTranslatorFactory(
						providerName);
				
				Properties constructionProps = new Properties();
				String value =
					connectionProperties.getProperty(variationProperty + "." +
						SGEQueue.QUEUE_SUPPORTED_SPMD_ADDITIONAL_CMDLINE_ARGS);
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
			null, SGE_MANAGER_TYPE);
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
	
	private class BulkSGEStatusFetcher implements BulkStatusFetcher
	{
		@Override
		public Map<JobToken, NativeQueueState> getStateMap()
				throws NativeQueueException
		{
			Map<JobToken, NativeQueueState> ret = 
				new HashMap<JobToken, NativeQueueState>();
			
			List<String> commandLine = new LinkedList<String>();
			commandLine.add(_qstatBinary.getAbsolutePath());
			for (String additionalArgString : _additionalArguments.qstatArguments())
				commandLine.add(additionalArgString);
			commandLine.add("-xml");
			
			if (_destination != null)
				commandLine.add(String.format("@%s", _destination));
			
			ProcessBuilder builder = new ProcessBuilder(commandLine);
			String result = execute(builder);
			Map<String, String> stateMap = SGEJobStatusParser.parseStatus(
				result);
			for (String tokenString : stateMap.keySet())
			{
				SGEJobToken token = new SGEJobToken(tokenString);
				SGEQueueState state = SGEQueueState.fromStateString(
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
			token, new BulkSGEStatusFetcher(), DEFAULT_CACHE_WINDOW);
		if (state == null)
			state = SGEQueueState.fromStateString("finished");
		return state;
	}

	@Override
	protected void generateQueueHeaders(PrintStream script,
			File workingDirectory, ApplicationDescription application)
			throws NativeQueueException, IOException
	{
		super.generateQueueHeaders(script, workingDirectory, application);
		
		String stdout = application.getStdoutRedirect();
		String stderr = application.getStderrRedirect();
		
		if (stdout != null)
			script.format("#$ -o %s\n", stdout);
		if (stderr != null)
			script.format("#$ -e %s\n", stderr);
		
		if (application.getSPMDVariation() != null)
		{
			throw new NativeQueueException(
				"SPMD not supported on SGE at the moment.");
		}
		
		ResourceConstraints resourceConstraints = 
			application.getResourceConstraints();
		if (resourceConstraints != null)
		{
			Double totalPhyscialMemory = 
				resourceConstraints.getTotalPhysicalMemory();
			if ( (totalPhyscialMemory != null) &&
				(!totalPhyscialMemory.equals(Double.NaN)) )
				script.format("#$ -l mf=%d\n", totalPhyscialMemory.longValue());
			
			Double wallclockTime = resourceConstraints.getWallclockTimeLimit();
			if (wallclockTime != null && !wallclockTime.equals(Double.NaN))
				script.format("#$ -l h_rt=%s\n", toWallTimeFormat(wallclockTime));
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
			throw new NativeQueueException("SPMD not supported in SGE at the moment.");
		} else
			super.generateApplicationBody(script, workingDirectory, application);
	}

	static final private Pattern JOB_TOKEN_PATTERN = Pattern.compile(
		"^.*Your job ([a-zA-Z0-9.]+)\\s+.*$");
	
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
		
		command.add("-wd");
		command.add(getWorkingDirectory().getAbsolutePath());
		
		for (String additionalArgString : _additionalArguments.qsubArguments())
			command.add(additionalArgString);
		
		command.add(submitScript.getAbsolutePath());
		
		ProcessBuilder builder = new ProcessBuilder(command);
		builder.directory(getWorkingDirectory());
		
		Map<Pattern, List<Matcher>> matches =
			ParsingExecutionEngine.executeAndParse(builder, JOB_TOKEN_PATTERN);
		List<Matcher> matchers = matches.get(JOB_TOKEN_PATTERN);
		if (matchers == null || matchers.size() < 1)
			throw new NativeQueueException(
				"qsub didn't result in a job ticket number being output.");
		if (matchers.size() > 1)
			throw new NativeQueueException(
				"qsub resulted in multiple job ticket numbers being output.");
		
		return new SGEJobToken(matchers.get(0).group(1).trim());
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