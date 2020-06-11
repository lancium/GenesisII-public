package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;
import java.util.Calendar;

import edu.virginia.vcgr.genii.client.byteio.RandomByteIORP;
import edu.virginia.vcgr.genii.client.byteio.StreamableByteIORP;
import edu.virginia.vcgr.genii.client.cmd.ReloadShellException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.rcreate.CreationException;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyManager;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.client.gpath.*;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;

public class TouchTool extends BaseGridTool
{
	static private final String _DESCRIPTION = "config/tooldocs/description/dtouch";

	static private final String _USAGE = "config/tooldocs/usage/utouch";

	static final private String _MANPAGE = "config/tooldocs/man/touch";

	public TouchTool()
	{

		super(new LoadFileResource(_DESCRIPTION), new LoadFileResource(_USAGE), true, ToolCategory.DATA);
		addManPage(new LoadFileResource(_MANPAGE));
	}

	@Override
	protected int runCommand() throws ReloadShellException, ToolException, UserCancelException, RNSException, AuthZSecurityException,
	IOException, ResourcePropertyException, CreationException
	{
		for (String arg : getArguments()) {
			RNSPath newPath = lookup(new GeniiPath(arg), RNSPathQueryFlags.DONT_CARE);
			// 2020-06-11 by ASG. Changed semantics to create the file if it does not exist.
			if (!newPath.exists()) {
				//System.out.println("file " + newPath.getName() + " does not exist\n");
				newPath.createNewFile();
			}
			// End of updates except the else { } for what was the code before.
			else {
				TypeInformation typeInfo = new TypeInformation(newPath.getEndpoint());
				if (typeInfo.isSByteIO()) {
					StreamableByteIORP rp =
							(StreamableByteIORP) ResourcePropertyManager.createRPInterface(newPath.getEndpoint(), StreamableByteIORP.class);
					rp.setModificationTime(Calendar.getInstance());
				} else if (typeInfo.isRByteIO()) {
					RandomByteIORP rp = (RandomByteIORP) ResourcePropertyManager.createRPInterface(newPath.getEndpoint(), RandomByteIORP.class);
					rp.setModificationTime(Calendar.getInstance());
				} else {
					throw new ToolException("Target path \"" + arg + "\" does not represent a ByteIO.");
				}
			}
		}

		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		for (String arg : getArguments()) {
			if (new GeniiPath(arg).pathType() != GeniiPathType.Grid) {
				throw new InvalidToolUsageException("rns-path must be a grid path. ");
			}
		}
	}
}