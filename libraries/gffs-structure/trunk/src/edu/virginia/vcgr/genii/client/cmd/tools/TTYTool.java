package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ReloadShellException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.rcreate.CreationException;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.client.tty.TTYConstants;
import edu.virginia.vcgr.genii.client.tty.TTYWatcher;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.gpath.GeniiPathType;

public class TTYTool extends BaseGridTool
{
	static private final String _DESCRIPTION = "config/tooldocs/description/dtty";
	static private final LoadFileResource _USAGE_RESOURCE = new LoadFileResource("config/tooldocs/usage/utty");
	static private final String _MANPAGE = "config/tooldocs/man/tty";

	static public final String WATCH_TOKEN = "watch";
	static public final String UNWATCH_TOKEN = "unwatch";

	public TTYTool()
	{
		super(new LoadFileResource(_DESCRIPTION), _USAGE_RESOURCE, false, ToolCategory.MISC);
		addManPage(new LoadFileResource(_MANPAGE));
	}

	@Override
	protected int runCommand() throws ReloadShellException, ToolException, UserCancelException, RNSException,
		AuthZSecurityException, IOException, ResourcePropertyException, CreationException
	{
		int numArgs = numArguments();
		if (numArgs == 1)
			unwatch();
		else
			watch(getArgument(1));
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		int numArgs = numArguments();
		if (numArgs == 1) {
			if (getArgument(0).equals(UNWATCH_TOKEN))
				return;
		} else if (numArgs == 2) {
			if (new GeniiPath(getArgument(1)).pathType() != GeniiPathType.Grid)
				throw new InvalidToolUsageException("<tty-object-path> must be a grid path. ");
			if (getArgument(0).equals(WATCH_TOKEN))
				return;
		}

		throw new InvalidToolUsageException();
	}

	public void watch(String path) throws RNSException, ToolException, FileNotFoundException, RemoteException, IOException
	{
		RNSPath rPath = lookup(new GeniiPath(path), RNSPathQueryFlags.MUST_EXIST);
		TypeInformation tInfo = new TypeInformation(rPath.getEndpoint());
		if (!tInfo.isTTY())
			throw new ToolException("Target path \"" + path + "\" is not a grid tty object.");

		TTYWatcher.watch(stdout, stderr, rPath.getEndpoint());
		ContextManager.getExistingContext().setSingleValueProperty(TTYConstants.TTY_CALLING_CONTEXT_PROPERTY,
			EPRUtils.toBytes(rPath.getEndpoint()));
	}

	public void unwatch() throws ToolException, IOException
	{
		TTYWatcher.unwatch();
		ContextManager.getExistingContext().removeProperty(TTYConstants.TTY_CALLING_CONTEXT_PROPERTY);
	}
}