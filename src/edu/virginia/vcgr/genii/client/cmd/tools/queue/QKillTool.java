package edu.virginia.vcgr.genii.client.cmd.tools.queue;

import java.util.ArrayList;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.cmd.tools.BaseGridTool;
import edu.virginia.vcgr.genii.client.queue.JobTicket;
import edu.virginia.vcgr.genii.client.queue.QueueManipulator;
import edu.virginia.vcgr.genii.client.gpath.*;

public class QKillTool extends BaseGridTool
{
	static final private String _DESCRIPTION = 
		"Kills a job already in the queue.";
	static final private String _USAGE =
		"qkill <queue-path> <ticket0>...<ticketn>";
	
	public QKillTool()
	{
		super(_DESCRIPTION, _USAGE, false);
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		GeniiPath gPath = new GeniiPath(getArgument(0));
		if(gPath.pathType() != GeniiPathType.Grid)
			throw new InvalidToolUsageException("<queue-path> must be a grid path. ");
		ArrayList<JobTicket> tickets = new ArrayList<JobTicket>(numArguments() - 1);
		QueueManipulator manipulator = new QueueManipulator(gPath.path());
		
		for (String arg : getArguments().subList(1, numArguments()))
		{
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