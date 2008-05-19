package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;

import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;

public class UnlinkTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"Unlinks (without descroying) the target paths.";
	static final private String _USAGE =
		"unlink <target-path> ...";
	
	public UnlinkTool()
	{
		super(_DESCRIPTION, _USAGE, false);
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		RNSPath path = RNSPath.getCurrent();
		for (int lcv = 0; lcv < numArguments(); lcv++)
		{
			unlink(path, getArgument(lcv));
		}
		
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() < 1)
			throw new InvalidToolUsageException();
	}
	
	static public void unlink(RNSPath currentPath,
		String filePath)
		throws RNSException, ConfigurationException, IOException
	{
		RNSPath file = currentPath.lookup(
			filePath, RNSPathQueryFlags.MUST_EXIST);
		
		file.unlink();
	}
}