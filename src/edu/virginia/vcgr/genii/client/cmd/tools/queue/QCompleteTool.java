package edu.virginia.vcgr.genii.client.cmd.tools.queue;

import java.util.ArrayList;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.cmd.tools.BaseGridTool;
import edu.virginia.vcgr.genii.client.queue.JobTicket;
import edu.virginia.vcgr.genii.client.queue.QueueManipulator;

public class QCompleteTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"Completes a job already in the queue (this job MUST be in a terminal state).";
	static final private String _USAGE =
		"qcomplete <queue-path> { --all | <ticket0>...<ticketn>}";
	
	private boolean _all = false;
	
	public QCompleteTool()
	{
		super(_DESCRIPTION, _USAGE, false);
	}
	
	public void setAll()
	{
		_all = true;
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		QueueManipulator manipulator = new QueueManipulator(getArgument(0));
		
		if (_all)
		{
			manipulator.completeAll();
		} else
		{
			ArrayList<JobTicket> tickets = new ArrayList<JobTicket>(numArguments() - 1);
		
			for (String arg : getArguments().subList(1, numArguments()))
			{
				tickets.add(new JobTicket(arg));
			}
			
			manipulator.complete(tickets);
		}
		
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		int numArgs = numArguments();
		if (numArgs <= 1 && !_all)
			throw new InvalidToolUsageException("Must indicate 1 or more tickets, or the --all flag.");
		if (numArgs != 1 && _all)
			throw new InvalidToolUsageException();
	}
}