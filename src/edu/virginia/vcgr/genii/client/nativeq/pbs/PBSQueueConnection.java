package edu.virginia.vcgr.genii.client.nativeq.pbs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.jsdl.JSDLUtils;
import edu.virginia.vcgr.genii.client.nativeq.ApplicationDescription;
import edu.virginia.vcgr.genii.client.nativeq.BasicResourceAttributes;
import edu.virginia.vcgr.genii.client.nativeq.FactoryResourceAttributes;
import edu.virginia.vcgr.genii.client.nativeq.JobToken;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueueException;
import edu.virginia.vcgr.genii.client.nativeq.NativeQueueState;
import edu.virginia.vcgr.genii.client.nativeq.ScriptBasedQueueConnection;
import edu.virginia.vcgr.genii.client.nativeq.ScriptExecutionException;
import edu.virginia.vcgr.genii.client.nativeq.ScriptLineParser;

public class PBSQueueConnection extends ScriptBasedQueueConnection
{
	@SuppressWarnings("unused")
	static private Log _logger = LogFactory.getLog(PBSQueueConnection.class);
	
	static final public URI PBS_MANAGER_TYPE = URI.create(
		"http://vcgr.cs.virginia.edu/genesisII/nativeq/pbs");
	
	private String _qName;
	
	private File _qsubBinary;
	private File _qstatBinary;
	private File _qdelBinary;
	
	PBSQueueConnection(File workingDirectory, Properties connectionProperties,
		String queueName, File qsubBinary, File qstatBinary, File qdelBinary)
			throws NativeQueueException
	{
		super(workingDirectory, connectionProperties);
		
		_qName = queueName;
		
		_qsubBinary = qsubBinary;
		_qstatBinary = qstatBinary;
		_qdelBinary = qdelBinary;
		
		checkBinary(_qsubBinary);
		checkBinary(_qsubBinary);
		checkBinary(_qsubBinary);
	}
	
	@Override
	protected void addSupportedSPMDVariations(Set<URI> variations,
			Properties connectionProperties)
	{
		super.addSupportedSPMDVariations(variations, connectionProperties);
		
		for (int lcv = 0; true; lcv++)
		{
			String variation = connectionProperties.getProperty(
				PBSQueue.QUEUE_SUPPORTED_SPMD_VARIATIONS_PROPERTY_BASE +
					"." + lcv);
			if (variation == null)
				break;
			
			variations.add(URI.create(variation));
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
		static private Pattern PATTERN =
			Pattern.compile("^\\s*job_state\\s*=\\s*(\\S+)\\s*$");
		
		private String _stateToken = null;
		
		@Override
		public Pattern[] getHandledPatterns()
		{
			return new Pattern[] { PATTERN };
		}

		@Override
		public void parseLine(Matcher matcher) throws NativeQueueException
		{
			_stateToken = matcher.group(1);
		}
		
		public String getStateToken()
			throws NativeQueueException
		{
			if (_stateToken == null)
				throw new NativeQueueException("Unable to parse qstat output.");
			
			return _stateToken;
		}
	}
	
	@Override
	public NativeQueueState getStatus(JobToken token)
			throws NativeQueueException
	{
		try
		{
			ProcessBuilder builder = new ProcessBuilder(
				_qstatBinary.getAbsolutePath(), "-f", token.toString());
			String result = execute(builder);
			JobStatusParser parser = new JobStatusParser();
			parseResult(result, parser);
			return PBSQueueState.fromStateSymbol(parser.getStateToken());
		}
		catch (ScriptExecutionException see)
		{
			if (see.getExitCode() == 153)
			{
				// This just means the job isn't in there -- probably exited
				return PBSQueueState.fromStateSymbol("E");
			}
			
			throw see;
		}
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
			if (numProcs != null)
			{
				script.format("#PBS -l nodes=%d\n", numProcs.intValue());
			}
		}
	}

	@Override
	protected void generateApplicationBody(PrintStream script,
			File workingDirectory, ApplicationDescription application)
			throws NativeQueueException, IOException
	{
		if (application.getSPMDVariation() != null)
		{
			script.format("cd \"%s\"\n", workingDirectory.getAbsolutePath());
			
			String execName = application.getExecutableName();
			if (!execName.contains("/"))
				execName = String.format("./%s", execName);
			
			script.format("mpiexec \"%s\"", execName);
			
			for (String arg : application.getArguments())
				script.format(" \"%s\"", arg);
			
			if (application.getStdinRedirect() != null)
				script.format(" < \"%s\"", application.getStdinRedirect());
			if (application.getStdoutRedirect() != null)
				script.format(" > \"%s\"", application.getStdoutRedirect());
			if (application.getStderrRedirect() != null)
				script.format(" 2> \"%s\"", application.getStderrRedirect());
			
			script.format("\nexport QUEUE_SCRIPT_RESULT=$?\n");
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