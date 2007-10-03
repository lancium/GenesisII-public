package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.cmd.tools.xscript.DefaultScriptHandler;
import edu.virginia.vcgr.genii.client.cmd.tools.xscript.XScriptRunner;

public class ScriptTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"Executes an XScript script.";
	
	static final private String _USAGE =
		"script [var=val ...] <script-file>";
	
	public ScriptTool()
	{
		super(_DESCRIPTION, _USAGE, false);
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
		File scriptFile = new File(scriptFileStr);
		if (!scriptFile.exists())
		{
			stderr.println("Couldn't locate script file \""
				+ scriptFile.getAbsolutePath() + "\".");
			return 1;
		}
		
		DefaultScriptHandler handler = new DefaultScriptHandler();
		return XScriptRunner.runScript(
			scriptFile, handler, stdout, stderr, stdin, initialProperties);
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 1)
			throw new InvalidToolUsageException();
	}
}