package edu.virginia.vcgr.genii.client.cmd.tools;

import java.util.Iterator;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.queue.JobInformation;
import edu.virginia.vcgr.genii.client.queue.QueueManipulator;

public class MonitorQueue extends BaseGridTool
{
	static private final String DESCRIPTION =
		"Monitors jobs in a queue until all are complete.";
	static private final String USAGE =
		"monitor-queue <queue-path> <sleep-interval-seconds>";
	
	public MonitorQueue()
	{
		super(DESCRIPTION, USAGE, true);
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		QueueManipulator manipulator = new QueueManipulator(getArgument(0));
		int finished;
		int total;
		long sleepInterval = Long.parseLong(getArgument(1)) * 1000L;
		
		while (true)
		{
			finished = total = 0;
			Iterator<JobInformation> info = manipulator.status(null);
			while (info.hasNext())
			{
				JobInformation jobInfo = info.next();
				total++;
				if (jobInfo.getJobState().isFinalState())
					finished++;
			}
			
			stdout.format("%d/%d completed.\n", finished, total);
			if (finished >= total)
				return 0;
			
			Thread.sleep(sleepInterval);
		}
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 2)
			throw new InvalidToolUsageException("Missing required arguments.");
	}
}