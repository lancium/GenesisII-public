package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Map;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.GridUserEnvironment;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;

public class CdTool extends BaseGridTool
{
	static final private String _DESCRIPTION = "edu/virginia/vcgr/genii/client/cmd/tools/description/dcd";
	static final private String _USAGE = "edu/virginia/vcgr/genii/client/cmd/tools/usage/ucd";
	static final private String _MANPAGE = "edu/virginia/vcgr/genii/client/cmd/tools/man/cd";

	public CdTool()
	{
		super(new FileResource(_DESCRIPTION), new FileResource(_USAGE), false, ToolCategory.DATA);
		addManPage(new FileResource(_MANPAGE));
	}

	@Override
	protected int runCommand() throws Throwable
	{
		chdir(getArgument(0));
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() == 0) {
			Map<String, String> env = GridUserEnvironment.getGridUserEnvironment();
			String value = env.get("HOME");
			if (value == null)
				throw new InvalidToolUsageException("\"HOME\" variable undefined.");

			addArgument(value);
		}

		if (numArguments() != 1)
			throw new InvalidToolUsageException();
	}

	static public void chdir(String target) throws ResourceException, RNSException, RemoteException, IOException,
		InvalidToolUsageException
	{
		RNSPath path = lookup(new GeniiPath(target), RNSPathQueryFlags.MUST_EXIST);
		TypeInformation typeInfo = new TypeInformation(path.getEndpoint());
		if (!typeInfo.isRNS())
			throw new RNSException("Path \"" + path.pwd() + "\" is not an RNS directory.");

		ICallingContext ctxt = ContextManager.getExistingContext();
		ctxt.setCurrentPath(path);
		ContextManager.storeCurrentContext(ctxt);
	}
}