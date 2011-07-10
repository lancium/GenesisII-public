package edu.virginia.vcgr.genii.client.cmd.tools;

import java.util.Collection;
import java.util.regex.Pattern;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.rns.filters.RNSFilter;
import edu.virginia.vcgr.genii.client.gpath.*;
import edu.virginia.vcgr.genii.client.io.FileResource;

public class ShellLoginTool extends BaseGridTool
{
	static private final String DESCRIPTION =
		"edu/virginia/vcgr/genii/client/cmd/tools/description/dshell-login";
	static private final String USAGE =
		"edu/virginia/vcgr/genii/client/cmd/tools/usage/ushell-login";
	static final private String _MANPAGE =
		"edu/virginia/vcgr/genii/client/cmd/tools/man/shell-login";
	
	public ShellLoginTool()
	{
		super(new FileResource(DESCRIPTION), new FileResource(USAGE), false,
				ToolCategory.SECURITY);
		addManPage(new FileResource(_MANPAGE));
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
		GeniiPath gPath = new GeniiPath(getArgument(0));
		if(gPath.pathType() != GeniiPathType.Grid)
		{
			throw new InvalidToolUsageException("<home-dir> must be a grid path. ");
		}
		for (RNSPath path : getLoginScripts(gPath.path()))
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