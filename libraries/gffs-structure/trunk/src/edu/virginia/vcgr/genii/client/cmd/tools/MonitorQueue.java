package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;
import java.util.Iterator;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ReloadShellException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.queue.JobInformation;
import edu.virginia.vcgr.genii.client.queue.QueueManipulator;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.client.gpath.*;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;

public class MonitorQueue extends BaseGridTool
{
	static private final String DESCRIPTION = "config/tooldocs/description/dmonitor-queue";
	static private final String USAGE = "config/tooldocs/usage/umonitor-queue";
	static final private String _MANPAGE = "config/tooldocs/man/monitor-queue";

	public MonitorQueue()
	{
		super(new LoadFileResource(DESCRIPTION), new LoadFileResource(USAGE), true, ToolCategory.INTERNAL);
		addManPage(new LoadFileResource(_MANPAGE));
	}

	@Override
	protected int runCommand() throws ReloadShellException, ToolException, UserCancelException, RNSException,
		AuthZSecurityException, IOException, ResourcePropertyException
	{
		GeniiPath gPath = new GeniiPath(getArgument(0));
		if (gPath.pathType() != GeniiPathType.Grid)
			throw new InvalidToolUsageException("queue-path must be a grid path. ");
		QueueManipulator manipulator = new QueueManipulator(gPath.path());
		int finished;
		int total;
		long sleepInterval = Long.parseLong(getArgument(1)) * 1000L;

		while (true) {
			finished = total = 0;
			Iterator<JobInformation> info = manipulator.status(null);
			while (info.hasNext()) {
				JobInformation jobInfo = info.next();
				total++;
				if (jobInfo.getJobState().isFinalState())
					finished++;
			}

			stdout.format("%d/%d completed.\n", finished, total);
			if (finished >= total)
				return 0;

			try {
				Thread.sleep(sleepInterval);
			} catch (InterruptedException e) {
				// nothing.
			}
		}
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 2)
			throw new InvalidToolUsageException("Missing required arguments.");
	}
}