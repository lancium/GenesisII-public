package edu.virginia.vcgr.genii.client.cmd.tools.queue;

import java.io.IOException;
import java.util.ArrayList;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ReloadShellException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.cmd.tools.BaseGridTool;
import edu.virginia.vcgr.genii.client.cmd.tools.ToolCategory;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.queue.JobTicket;
import edu.virginia.vcgr.genii.client.queue.QueueManipulator;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.client.gpath.*;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;

public class QKillTool extends BaseGridTool
{
	static final private String _DESCRIPTION = "config/tooldocs/description/dqkill";
	static final private String _USAGE = "config/tooldocs/usage/uqkill";
	static final private String _MANPAGE = "config/tooldocs/man/qkill";

	public QKillTool()
	{
		super(new LoadFileResource(_DESCRIPTION), new LoadFileResource(_USAGE), false, ToolCategory.EXECUTION);
		addManPage(new LoadFileResource(_MANPAGE));
	}

	@Override
	protected int runCommand() throws ReloadShellException, ToolException, UserCancelException, RNSException,
		AuthZSecurityException, IOException, ResourcePropertyException
	{
		GeniiPath gPath = new GeniiPath(getArgument(0));
		if (gPath.pathType() != GeniiPathType.Grid)
			throw new InvalidToolUsageException("<queue-path> must be a grid path. ");
		ArrayList<JobTicket> tickets = new ArrayList<JobTicket>(numArguments() - 1);
		QueueManipulator manipulator = new QueueManipulator(gPath.path());

		for (String arg : getArguments().subList(1, numArguments())) {
			tickets.add(new JobTicket(arg));
		}

		manipulator.kill(tickets);

		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() < 2)
			throw new InvalidToolUsageException("Must supply a queue path and at least 1 job ticket.");
	}
}