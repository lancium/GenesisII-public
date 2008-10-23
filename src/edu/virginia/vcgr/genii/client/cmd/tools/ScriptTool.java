package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.byteio.ByteIOStreamFactory;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.cmd.tools.xscript.jsr.Grid;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;

public class ScriptTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"Executes a script.";
	
	static final private String _USAGE =
		"script [var=val ...] <script-file>";
	
	public ScriptTool()
	{
		super(_DESCRIPTION, _USAGE, false);
	}
	
	static private String getExtension(String filename)
	{
		int index = filename.lastIndexOf('.');
		if (index <= 0)
			return "xml";
		
		return filename.substring(index + 1);
	}
	
	static private Reader openReader(String path) throws IOException
	{
		try
		{
			if (path.startsWith("rns:"))
			{
				return new InputStreamReader(
					ByteIOStreamFactory.createInputStream(
						RNSPath.getCurrent().lookup(path.substring(4)), true));
			} else
			{
				return new FileReader(path);
			}
		}
		catch (RNSPathDoesNotExistException rpdnee)
		{
			throw new FileNotFoundException(String.format(
				"The path %s does not exist.", path));
		}
		catch (RNSException rne)
		{
			throw new IOException(String.format(
				"Unable to open file %s.", path), rne);
		}
	}
	
	@Override
	protected int runCommand() throws Throwable
	{	
		int lcv;
		Properties initialProperties = new Properties();
		
		for (Object key : System.getProperties().keySet())
		{
			String sKey = (String)key;
			initialProperties.setProperty(sKey, System.getProperties().getProperty(sKey));
		}
		
		Map<String, String> env = System.getenv();
		for (String key : env.keySet())
		{
			initialProperties.put("ENV." + key, env.get(key));
		}
		
		List<String> args = getArguments();
		for (lcv = 0; lcv < args.size(); lcv++)
		{
			String arg = args.get(lcv);
			int index = arg.indexOf('=');
			if (index < 0)
				break;
			initialProperties.put(arg.substring(0, index), arg.substring(index + 1));
		}
		
		String scriptFileStr = getArgument(lcv);
		
		String []cArgs = new String[args.size() - lcv];
		int start = lcv;
		for (;lcv < args.size(); lcv++)
			cArgs[lcv - start] = args.get(lcv);
		
		String extension = getExtension(scriptFileStr);
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByExtension(extension);
		engine.put("grid", new Grid(
			initialProperties, stdin, stdout, stderr));
		Bindings b = engine.getBindings(ScriptContext.GLOBAL_SCOPE);
		b.put("ARGV", cArgs);
		for (Object property : initialProperties.keySet())
			b.put((String)property, 
				initialProperties.get(property));
			
		Reader reader = null;
		try
		{	
			reader = openReader(scriptFileStr);
			engine.eval(reader);
			return 0;
		}
		catch (ScriptException se)
		{
			Throwable cause = se.getCause();
			if (cause != null)
				throw cause;
			
			throw se;
		}
		finally
		{
			StreamUtils.close(reader);
		}
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() < 1)
			throw new InvalidToolUsageException();
	}
}