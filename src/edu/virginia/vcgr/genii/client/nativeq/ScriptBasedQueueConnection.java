package edu.virginia.vcgr.genii.client.nativeq;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

public abstract class ScriptBasedQueueConnection 
	extends AbstractNativeQueueConnection
{
	static private Log _logger = LogFactory.getLog(
		ScriptBasedQueueConnection.class);
	
	static public final String QUEUE_SCRIPT_RESULT_FILENAME = 
		"queue.script.result";
	
	static private final String DEFAULT_BASH_BINARY_PATH = "/bin/bash";
	
	private String _submitScriptName;
	private File _bashBinary;
	
	protected ScriptBasedQueueConnection(File workingDirectory,
		Properties connectionProperties)
		throws NativeQueueException
	{
		super(workingDirectory, connectionProperties);
	}
	
	@Override
	protected void initialize(Properties connectionProperties)
		throws NativeQueueException
	{
		super.initialize(connectionProperties);
		
		_submitScriptName = connectionProperties.getProperty(
			NativeQueue.SUBMIT_SCRIPT_NAME_PROPERTY);
		
		if (_submitScriptName != null && _submitScriptName.contains("/"))
			throw new IllegalArgumentException(
				"SubmitScriptName must not contain any " +
				"path characters (such as '/').");
		
		_bashBinary = new File(connectionProperties.getProperty(
			NativeQueue.BASH_BINARY_PATH_PROPERTY, DEFAULT_BASH_BINARY_PATH));
		checkBinary(_bashBinary);	
	}
	
	final protected File getSubmitScript(File workingDirectory)
		throws IOException
	{
		if (_submitScriptName == null)
			return File.createTempFile("qsub", ".sh", workingDirectory);
		
		return new File(workingDirectory, _submitScriptName);
	}
	
	final protected File getBashBinary()
	{
		return _bashBinary;
	}
	
	final protected File generateSubmitScript(File workingDirectory,
		ApplicationDescription application) throws NativeQueueException
	{
		PrintStream ps = null;
		
		try
		{
			File submitScript = getSubmitScript(workingDirectory);
			
			ps = new PrintStream(submitScript);
			generateScriptHeader(ps, workingDirectory, application);
			generateQueueHeaders(ps, workingDirectory, application);
			generateApplicationBody(ps, workingDirectory, application);
			generateScriptFooter(ps, workingDirectory, application);
			
			submitScript.setExecutable(true, true);
			return submitScript;
		}
		catch (IOException ioe)
		{
			throw new NativeQueueException(
				"Unable to generate submit script.", ioe);
		}
		finally
		{
			StreamUtils.close(ps);
		}
	}
	
	protected void generateScriptHeader(PrintStream script,
		File workingDirectory, ApplicationDescription application)
			throws NativeQueueException, IOException
	{
		script.format("#%s\n\n", getBashBinary().getAbsolutePath());
		script.format("export QUEUE_SCRIPT_RESULT=0\n");
	}
	
	protected void generateQueueHeaders(PrintStream script,
		File workingDirectory, ApplicationDescription application)
			throws NativeQueueException, IOException
	{
	}
	
	protected void generateApplicationBody(PrintStream script,
		File workingDirectory, ApplicationDescription application)
			throws NativeQueueException, IOException
	{
		script.format("cd \"%s\"\n", workingDirectory.getAbsolutePath());
		
		script.format("\"%s\"", application.getExecutableName());
		
		for (String arg : application.getArguments())
			script.format(" \"%s\"", arg);
		
		if (application.getStdinRedirect() != null)
			script.format(" < \"%s\"", application.getStdinRedirect());
		if (application.getStdoutRedirect() != null)
			script.format(" > \"%s\"", application.getStdoutRedirect());
		if (application.getStderrRedirect() != null)
			script.format(" 2> \"%s\"", application.getStderrRedirect());
		
		script.format("\nexport QUEUE_SCRIPT_RESULT=$?\n");
	}
	
	protected void generateScriptFooter(PrintStream script,
		File workingDirectory, ApplicationDescription application)
			throws NativeQueueException, IOException
	{
		script.format("echo $QUEUE_SCRIPT_RESULT > %s.tmp\n", 
			QUEUE_SCRIPT_RESULT_FILENAME);
		script.format("mv %1$s.tmp %1$s\n", QUEUE_SCRIPT_RESULT_FILENAME);
		script.println("exit $QUEUE_SCRIPT_RESULT");
	}
	
	protected String execute(ProcessBuilder builder)
		throws NativeQueueException
	{
		try
		{
			Collection<String> commandLine = builder.command();
			logProcess(commandLine);
			
			Process proc = builder.start();
			proc.getOutputStream().close();
			StreamCopier stdoutCopy = new StreamCopier(proc.getInputStream());
			StreamCopier stderrCopy = new StreamCopier(proc.getErrorStream());
			int result = proc.waitFor();
			
			logProcessResult(result, stdoutCopy, stderrCopy);
			
			if (result == 0)
				return stdoutCopy.getResult();
		
			throw new ScriptExecutionException(
				commandLine, result, stderrCopy.getResult());
		}
		catch (InterruptedException ie)
		{
			throw new NativeQueueException("Unable to execute command.", ie);
		}
		catch (IOException ioe)
		{
			throw new NativeQueueException("Unable to execute command.", ioe);
		}
	}
	
	static private class StreamCopier implements Runnable
	{
		private Thread _myThread;
		private InputStream _source;
		private ByteArrayOutputStream _sink;
		private IOException _exception = null;
		
		public StreamCopier(InputStream source)
		{
			_source = source;
			_sink = new ByteArrayOutputStream();
			
			_myThread = new Thread(this);
			_myThread.setDaemon(true);
			_myThread.setName("Stream Copier");
			
			_myThread.start();
		}
		
		public void run()
		{
			try
			{
				StreamUtils.copyStream(_source, _sink);
			}
			catch (IOException ioe)
			{
				_exception = ioe;
			}
		}
		
		public String getResult()
			throws IOException
		{
			try
			{
				_myThread.join();
				if (_exception != null)
					throw _exception;
				
				return new String(_sink.toByteArray());
			}
			catch (InterruptedException ie)
			{
				return null;
			}
		}
	}
	
	protected void parseResult(String result, ScriptLineParser...parsers)
		throws NativeQueueException
	{
		BufferedReader reader = new BufferedReader(new StringReader(result));
		String line;
		
		try
		{
			while ( (line = reader.readLine()) != null)
			{
				for (ScriptLineParser parser : parsers)
				{
					for (Pattern pattern : parser.getHandledPatterns())
					{
						Matcher matcher = pattern.matcher(line);
						if (matcher.matches())
						{
							parser.parseLine(matcher);
							break;
						}
					}
				}
			}
		}
		catch (IOException ioe)
		{
			// This won't happen.
		}
	}
	
	static private void logProcess(Collection<String> commandLine)
	{
		_logger.debug(new LazyEvaluatorList(commandLine));
	}
	
	static private void logProcessResult(int result,
		StreamCopier stdout, StreamCopier stderr)
	{
		_logger.debug(new LazyEvaluatorResult(result, stdout, stderr));
	}
	
	static private class LazyEvaluatorResult
	{
		private int _result;
		private StreamCopier _stdout;
		private StreamCopier _stderr;
		
		public LazyEvaluatorResult(int result,
			StreamCopier stdout, StreamCopier stderr)
		{
			_result = result;
			_stdout = stdout;
			_stderr = stderr;
		}
		
		public String toString()
		{
			try
			{
				StringBuilder builder = new StringBuilder("Result:  " + _result + "\n");
				builder.append("Standard Out:\n");
				builder.append(_stdout.getResult());
				builder.append("\nStandard Err:\n");
				builder.append(_stderr.getResult());
				
				return builder.toString();
			}
			catch (IOException ioe)
			{
				return "Unable to get result from stream:  " 
					+ ioe.getLocalizedMessage();
			}
		}
	}
	
	static private class LazyEvaluatorList
	{
		private Collection<String> _list;
		
		public LazyEvaluatorList(Collection<String> list)
		{
			_list = list;
		}
		
		public String toString()
		{
			StringBuilder builder = new StringBuilder("Executing:  ");
			
			boolean first = true;
			for (String entry : _list)
			{
				if (!first)
					builder.append(" ");
				first = false;
				builder.append("\"" + entry + "\"");
			}
			
			return builder.toString();
		}
	}
}