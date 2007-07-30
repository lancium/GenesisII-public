package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.File;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.cmd.tools.xscript.DefaultScriptHandler;
import edu.virginia.vcgr.genii.client.cmd.tools.xscript.XScriptRunner;

public class ScriptTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"Executes an XScript script.";
	static final private String _USAGE =
		"script <script-file>";
	
	public ScriptTool()
	{
		super(_DESCRIPTION, _USAGE, false);
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		String scriptFileStr = getArgument(0);
		File scriptFile = new File(scriptFileStr);
		if (!scriptFile.exists())
		{
			stderr.println("Couldn't locate script file \""
				+ scriptFile.getAbsolutePath() + "\".");
			return 1;
		}
		
		DefaultScriptHandler handler = new DefaultScriptHandler();
		return XScriptRunner.runScript(
			scriptFile, handler, stdout, stderr, stdin);
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 1)
			throw new InvalidToolUsageException();
	}
}