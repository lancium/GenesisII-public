package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;
import java.util.Collection;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ReloadShellException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.rcreate.CreationException;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.rns.filters.RNSFilter;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.client.gpath.*;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;

public class ShellLoginTool extends BaseGridTool
{
	static private Log _logger = LogFactory.getLog(ShellLoginTool.class);

	static private final String DESCRIPTION = "config/tooldocs/description/dshell-login";
	static private final String USAGE = "config/tooldocs/usage/ushell-login";
	static final private String _MANPAGE = "config/tooldocs/man/shell-login";

	public ShellLoginTool()
	{
		super(new LoadFileResource(DESCRIPTION), new LoadFileResource(USAGE), false, ToolCategory.SECURITY);
		addManPage(new LoadFileResource(_MANPAGE));
	}

	static private Pattern LOGIN_FILENAME_PATTERN = Pattern.compile("^\\.glogin\\.[a-zA-Z0-9]+$");

	private Collection<RNSPath> getLoginScripts(String homeDirString) throws RNSException
	{
		RNSPath homeDir = RNSPath.getCurrent().lookup(homeDirString, RNSPathQueryFlags.MUST_EXIST);
		return homeDir.listContents(new RNSFilter()
		{
			@Override
			public boolean matches(RNSPath testEntry)
			{
				return LOGIN_FILENAME_PATTERN.matcher(testEntry.getName()).matches();
			}
		});
	}

	@Override
	protected int runCommand() throws ReloadShellException, ToolException, UserCancelException, RNSException,
		AuthZSecurityException, IOException, ResourcePropertyException, CreationException
	{
		GeniiPath gPath = new GeniiPath(getArgument(0));
		if (gPath.pathType() != GeniiPathType.Grid) {
			throw new InvalidToolUsageException("<home-dir> must be a grid path. ");
		}
		for (RNSPath path : getLoginScripts(gPath.path())) {
			ScriptTool tool = new ScriptTool();
			tool.addArgument("rns:" + path.pwd());
			int retVal = tool.run(stdout, stderr, stdin);
			if (retVal != 0) {
				String msg = "failure during login script: return value=" + retVal;
				_logger.error(msg);
			}
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