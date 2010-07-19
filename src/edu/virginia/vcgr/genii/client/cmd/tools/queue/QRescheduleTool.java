package edu.virginia.vcgr.genii.client.cmd.tools.queue;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.cmd.tools.BaseGridTool;
import edu.virginia.vcgr.genii.client.queue.QueueManipulator;

public class QRescheduleTool extends BaseGridTool {

	static private final String _DESCRIPTION = 
		"Returns a running job to the queue and ensures it is not rescheduled on same bes " +
		" Warning: Must manually reset slot count for this resource later.";
	static private final String _USAGE =
		"qreschedule <queue-path> <resource-name> <ticket0>...<ticketn>";
	
	public QRescheduleTool()
	{
		super(_DESCRIPTION, _USAGE, false);
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		QueueManipulator manipulator = new QueueManipulator(getArgument(0));
		String[] tickets = new String[numArguments()-2];
		
		System.arraycopy(getArguments(), 2, tickets, 0, numArguments() - 2);
		
		/* Andrew actually doesn't want us to do this in the tool, he wants to
		 * do it outside the tool.
		 */
		// manipulator.configure(getArgument(1), 0);
		
		manipulator.rescheduleJobs(tickets);
		
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() < 3)
			throw new InvalidToolUsageException("Must supply a queue path, resource name, and at least 1 job ticket.");
	}
}
