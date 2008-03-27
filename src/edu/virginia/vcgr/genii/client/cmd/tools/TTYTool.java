package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;

import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.tty.TTYConstants;
import edu.virginia.vcgr.genii.client.tty.TTYException;
import edu.virginia.vcgr.genii.client.tty.TTYWatcher;

public class TTYTool extends BaseGridTool
{
	static private final String _DESCRIPTION = 
		"Creates, manipulates, and destroys grid tty objects.";
	static private final FileResource _USAGE_RESOURCE =	new FileResource(
		"edu/virginia/vcgr/genii/client/cmd/tools/resources/tty-usage.txt");

	static public final String WATCH_TOKEN = "watch";
	static public final String UNWATCH_TOKEN = "unwatch";
	
	public TTYTool()
	{
		super(_DESCRIPTION, _USAGE_RESOURCE, false);
	}
	
	@Override
	protected int runCommand() throws Throwable
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
		if (numArgs == 1)
		{
			if (getArgument(0).equals(UNWATCH_TOKEN))
				return;
		} else if (numArgs == 2)
		{
			if (getArgument(0).equals(WATCH_TOKEN))
				return;
		}
		
		throw new InvalidToolUsageException();
	}
	
	public void watch(String path) 
		throws RNSException, ConfigurationException, 
			ToolException, TTYException, FileNotFoundException,
			RemoteException, IOException
	{
		RNSPath rPath = RNSPath.getCurrent().lookup(
			path, RNSPathQueryFlags.MUST_EXIST);
		TypeInformation tInfo = new TypeInformation(rPath.getEndpoint());
		if (!tInfo.isTTY())
			throw new ToolException("Target path \"" + path + 
				"\" is not a grid tty object.");
		
		TTYWatcher.watch(stdout, stderr, rPath.getEndpoint());
		ContextManager.getCurrentContext().setSingleValueProperty(
			TTYConstants.TTY_CALLING_CONTEXT_PROPERTY, EPRUtils.toBytes(rPath.getEndpoint()));
	}
	
	public void unwatch() throws TTYException, IOException, ConfigurationException
	{
		TTYWatcher.unwatch();
		ContextManager.getCurrentContext().removeProperty(
			TTYConstants.TTY_CALLING_CONTEXT_PROPERTY);
	}
}