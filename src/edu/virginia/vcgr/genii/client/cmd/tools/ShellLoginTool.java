package edu.virginia.vcgr.genii.client.cmd.tools;

import java.util.Collection;
import java.util.regex.Pattern;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.rns.filters.RNSFilter;

public class ShellLoginTool extends BaseGridTool
{
	static private final String DESCRIPTION =
		"Log's the user into his home directory (looks for a script called .glogin.<extension> there.).";
	static private final String USAGE =
		"shell-login <home-dir>.";
	
	public ShellLoginTool()
	{
		super(DESCRIPTION, USAGE, false);
	}
	
	static private Pattern LOGIN_FILENAME_PATTERN = Pattern.compile(
		"^\\.glogin\\.[a-zA-Z0-9]+$");
	
	private Collection<RNSPath> getLoginScripts(String homeDirString)
		throws Throwable
	{
		RNSPath homeDir = RNSPath.getCurrent().lookup(homeDirString,
			RNSPathQueryFlags.MUST_EXIST);
		return homeDir.listContents(new RNSFilter() {
			@Override
			public boolean matches(RNSPath testEntry)
			{
				return LOGIN_FILENAME_PATTERN.matcher(
					testEntry.getName()).matches();
			}
		});
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		for (RNSPath path : getLoginScripts(getArgument(0)))
		{
			ScriptTool tool = new ScriptTool();
			tool.addArgument("rns:" + path.pwd());
			tool.run(stdout, stderr, stdin);
		}
		
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 1)
			throw new InvalidToolUsageException();
	}
}