package edu.virginia.vcgr.externalapp;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.MacroUtils;
import org.morgan.util.io.StreamUtils;

public class DefaultExternalApplication extends AbstractExternalApplication
{
	static private Log _logger = LogFactory.getLog(
		DefaultExternalApplication.class);
	
	static private AtomicLong _instanceId = new AtomicLong(0);
	
	private Properties _macroResolution;
	
	private long _instanceNumber;
	private String _description;
	private List<String> _predefinedCommandLine;
	private File _workingDirectory = null;
	private Map<String, String> _environmentOverload =
		new HashMap<String, String>();
	
	protected void readOutput(InputStream stream)
	{
		try
		{
			BufferedReader reader = new BufferedReader(
				new InputStreamReader(stream));
			
			String line;
			while ( (line = reader.readLine()) != null)
			{
				_logger.info(String.format(
					"[%x] External Application Output [%s]:  %s",
					_instanceNumber, this, line));
			}
		}
		catch (Throwable cause)
		{
			_logger.warn(
				"Error trying to read output/error stream from application.",
				cause);
		}
		finally
		{
			StreamUtils.close(stream);
		}
	}
	
	protected void doRun(File content) throws Throwable
	{
		List<String> commandLine = new ArrayList<String>(
			_predefinedCommandLine.size()); 
		
		for (String orig : _predefinedCommandLine)
			commandLine.add(MacroUtils.replaceMacros(_macroResolution, 
				String.format(orig, content.getAbsolutePath())));
		
		ProcessBuilder builder = new ProcessBuilder(commandLine);
		if (_workingDirectory != null)
			builder.directory(_workingDirectory);
		
		if (_environmentOverload.size() > 0)
		{
			Map<String, String> builderEnv = builder.environment();
			
			for (Map.Entry<String, String> entry : 
				_environmentOverload.entrySet())
				builderEnv.put(entry.getKey(), entry.getValue());
		}
		
		builder.redirectErrorStream(true);
		Process process = builder.start();
		StreamUtils.close(process.getOutputStream());
		
		readOutput(process.getInputStream());
		
		try
		{
			process.waitFor();
		}
		catch (InterruptedException ie)
		{
			process.destroy();
			throw ie;
		}
	}
	
	public DefaultExternalApplication(String description, 
		String...predefinedCommandLine)
	{
		if (description == null)
			throw new IllegalArgumentException(
				"DefaultExternalApplication MUST have a description.");
		
		if (predefinedCommandLine.length == 0)
			throw new IllegalArgumentException(
				"Command line must have at least one token.");
		
		_macroResolution = new Properties();
		for (Map.Entry<String, String> entry : System.getenv().entrySet())
			_macroResolution.setProperty(entry.getKey(), entry.getValue());
		
		_instanceNumber = _instanceId.getAndAdd(1);
		
		_description = description;
		_predefinedCommandLine = Arrays.asList(predefinedCommandLine);
	}
	
	@Override
	public String toString()
	{
		return MacroUtils.replaceMacros(_macroResolution, _description);
	}
}