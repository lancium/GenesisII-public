package edu.virginia.vcgr.genii.client.cmd.tools;

import java.util.Calendar;

import edu.virginia.vcgr.genii.client.byteio.RandomByteIORP;
import edu.virginia.vcgr.genii.client.byteio.StreamableByteIORP;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyManager;
import edu.virginia.vcgr.genii.client.gpath.*;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;

public class TouchTool extends BaseGridTool
{
	static private final String _DESCRIPTION =
		"Update the modification time on a set of target ByteIO instances.";
	
	static private final String _USAGE =
		"touch [rns-path1 ... rns-pathn]";

	public TouchTool()
	{
		super(_DESCRIPTION, _USAGE, true);
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		for (String arg : getArguments())
		{
			RNSPath newPath = lookup(new GeniiPath(arg), RNSPathQueryFlags.MUST_EXIST);
			TypeInformation typeInfo = new TypeInformation(newPath.getEndpoint());
			if (typeInfo.isSByteIO())
			{
				StreamableByteIORP rp = 
					(StreamableByteIORP)ResourcePropertyManager.createRPInterface(
						newPath.getEndpoint(), StreamableByteIORP.class);
				rp.setModificationTime(Calendar.getInstance());
			} else if (typeInfo.isRByteIO())
			{
				RandomByteIORP rp =
					(RandomByteIORP)ResourcePropertyManager.createRPInterface(
						newPath.getEndpoint(), RandomByteIORP.class);
				rp.setModificationTime(Calendar.getInstance());
			} else
			{
				throw new ToolException("Target path \"" + arg + 
					"\" does not represent a ByteIO.");
			}
		}
		
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		for(String arg : getArguments())
		{
			if(new GeniiPath(arg).pathType() != GeniiPathType.Grid)
			{
				throw new InvalidToolUsageException("rns-path must be a grid path. ");
			}
		}
	}
}