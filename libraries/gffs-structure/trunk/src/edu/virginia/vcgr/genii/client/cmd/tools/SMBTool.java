package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ReloadShellException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.dialog.DialogException;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;
import edu.virginia.vcgr.genii.client.rcreate.CreationException;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.security.GenesisIISecurityException;
import edu.virginia.vcgr.smb.server.SMBServer;

public class SMBTool extends BaseGridTool
{

	static final private String _DESCRIPTION = "config/tooldocs/description/dsmb";
	static final private String _USAGE = "config/tooldocs/usage/usmb";
	static final private String _MANPAGE = "config/tooldocs/man/smb";

	static private SMBServer server = new SMBServer(3333);

	public SMBTool()
	{
		super(new LoadFileResource(_DESCRIPTION), new LoadFileResource(_USAGE), false, ToolCategory.DATA);
		addManPage(new LoadFileResource(_MANPAGE));
	}

	@Override
	protected void verify() throws ToolException
	{
		if (getArguments().size() != 1)
			throw new InvalidToolUsageException();
	}

	@Override
	protected int runCommand() throws ReloadShellException, ToolException, UserCancelException, RNSException, GenesisIISecurityException,
		IOException, ResourcePropertyException, CreationException, ClassNotFoundException, DialogException
	{
		String action = getArguments().get(0);
		if (action.equalsIgnoreCase("start"))
			server.start();
		else if (action.equalsIgnoreCase("stop"))
			server.stop();
		else
			throw new InvalidToolUsageException();

		return 0;
	}

}
