package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ContextStreamUtils;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.resource.ResourceException;

public class ConnectTool extends BaseGridTool
{
	static private final String _DESCRIPTION =
		"Connects to an existing net.";
	static private final String _USAGE =
		"connect <connect-url>";
	
	public ConnectTool()
	{
		super(_DESCRIPTION, _USAGE, false);
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		String connectURL = getArgument(0);
		connect(connectURL);
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 1)
			throw new InvalidToolUsageException();
	}
	

	public void connect(ICallingContext ctxt)
		throws ConfigurationException, ResourceException, IOException
	{
		ContextManager.storeCurrentContext(ctxt);
	}
	
	public void connect(String connectURL)
		throws ResourceException, MalformedURLException, IOException,
			ConfigurationException
	{
		URL url = new URL(connectURL);
		connect(ContextStreamUtils.load(url));
	}
}