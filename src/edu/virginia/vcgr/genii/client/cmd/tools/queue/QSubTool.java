package edu.virginia.vcgr.genii.client.cmd.tools.queue;

import java.io.File;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.cmd.tools.BaseGridTool;
import edu.virginia.vcgr.genii.client.queue.JobTicket;
import edu.virginia.vcgr.genii.client.queue.QueueManipulator;

public class QSubTool extends BaseGridTool
{
	static private final String _DESCRIPTION =
		"Submits a new job to a given Queue.";
	static private final String _USAGE =
		"qsub <queue-path> [--priority=<priority>] <jsdl-file>";
	
	private int _priority = 0;
	
	public QSubTool()
	{
		super(_DESCRIPTION, _USAGE, false);
	}
	
	public void setPriority(String priority)
		throws ToolException
	{
		_priority = Integer.parseInt(priority);
		if (_priority < -10 || _priority > 10)
			throw new InvalidToolUsageException("Priority must be between -10 and 10.");
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		String queuePath = getArgument(0);
		String jsdlFile = getArgument(1);
		
		QueueManipulator manipulator = new QueueManipulator(queuePath);
		JobTicket ticket = manipulator.submit(new File(jsdlFile), _priority);
		
		stdout.println("Job Submitted.  Ticket is \"" + ticket + "\".");
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 2)
			throw new InvalidToolUsageException();
	}
}