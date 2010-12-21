package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.cmd.tools.xscript.jsr.Grid;
import edu.virginia.vcgr.genii.client.configuration.GridEnvironment;
import edu.virginia.vcgr.genii.client.configuration.PathVariable;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.gpath.GeniiPathType;

public class ScriptTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"Executes a script.";
	
	static final private String _USAGE =
		"script [--global-properties=<properties-file>] [var=val ...] [--language=<language>] [<script-file>]";
	
	@Option("language")
	private String _language = null;
	
	private Collection<String> _globalPropertiesPaths =
		new LinkedList<String>();
	
	private ScriptEngine scriptEngine(GeniiPath path)
	{
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = null;
		
		if (path == null || _language != null)
		{
			if (_language == null)
				stderr.format("Must supply either a language, or a script!\n");
			else
			{
				for (ScriptEngineFactory factory : manager.getEngineFactories())
				{
					if (factory.getLanguageName().equals(_language))
					{
						engine = factory.getScriptEngine();
						engine.setBindings(manager.getBindings(),
							ScriptContext.GLOBAL_SCOPE);
						break;
					}
				}
				
				if (engine == null)
				{
					stderr.format(
						"No scripting engine registered for language %s!\n", 
						_language);
					stderr.format("Valid languages include:  ");
					boolean first = true;
					for (ScriptEngineFactory factory : manager.getEngineFactories())
					{
						if (!first)
							stderr.format(", ");
						first = false;
						
						stderr.print(factory.getLanguageName());
					}
					stderr.println();
				}
			}
		} else
		{
			String extension = getExtension(path.path());
			engine = manager.getEngineByExtension(extension);
			
			if (engine == null)
				stderr.format("No scripting engine registered for extension %s!\n",
					extension);
		}

		return engine;
	}
	
	public void setGlobal_properties(String propertiesPath)
	{
		_globalPropertiesPaths.add(propertiesPath);
	}
	
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
	
	static private Reader openReader(GeniiPath path) throws IOException
	{
		return new InputStreamReader(path.openInputStream());
	}
	
	static private Properties loadProperties(String propertiesPath) 
		throws IOException
	{
		Properties ret = new Properties();
		InputStream in = null;
		
		try
		{
			GeniiPath path = new GeniiPath(propertiesPath);
			in = path.openInputStream();
			ret.load(in);
			return ret;
		}
		finally
		{
			StreamUtils.close(in);
		}
	}
	
	@Override
	protected int runCommand() throws Throwable
	{	
		Reader reader = null;
		int lcv;
		Properties initialProperties = new Properties();
		
		for (String propertiesPath : _globalPropertiesPaths)
		{
			Properties props = loadProperties(propertiesPath);
			for (Object propName : props.keySet())
			{
				initialProperties.setProperty(
					propName.toString(), props.getProperty(
						propName.toString()));
			}
		}
		
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
		
		try
		{
			ScriptEngine engine;
			if (lcv >= args.size())
			{
				// Read from stdin
				engine = scriptEngine(null);
				if (engine == null)
					return -1;
			} else
			{
				GeniiPath scriptFilePath = new GeniiPath(getArgument(lcv));
				if (scriptFilePath.pathType() == GeniiPathType.Local)
				{
					File scriptFile = PathVariable.lookupVariable(System.getProperties(), 
						GridEnvironment.GRID_PATH_ENV_VARIABLE).find(
							scriptFilePath.path(),
							PathVariable.FindTypes.FILE);
					if (scriptFile == null)
						throw new FileNotFoundException(String.format(
							"Unable to locate script file %s.", scriptFilePath));
					scriptFilePath = new GeniiPath(
						"local:" + scriptFile.getAbsolutePath());
				}
				
				engine = scriptEngine(scriptFilePath);
				if (engine == null)
					return -1;
				
				reader = openReader(scriptFilePath);
			}
				
			String []cArgs = new String[args.size() - lcv];
			int start = lcv;
			for (;lcv < args.size(); lcv++)
				cArgs[lcv - start] = args.get(lcv);	
			
			engine.put("grid", new Grid(
				initialProperties, stdin, stdout, stderr));
			Bindings b = engine.getBindings(ScriptContext.GLOBAL_SCOPE);
			b.put("ARGV", cArgs);
	
			for (Object property : initialProperties.keySet())
				b.put((String)property, 
					initialProperties.get(property));
					
			if (reader != null)
				engine.eval(reader);
			else
				engine.eval(stdin);
			
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
	}
}