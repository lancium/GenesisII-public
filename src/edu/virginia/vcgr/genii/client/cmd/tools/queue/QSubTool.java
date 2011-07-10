package edu.virginia.vcgr.genii.client.cmd.tools.queue;

import java.io.File;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.cmd.tools.BaseGridTool;
import edu.virginia.vcgr.genii.client.cmd.tools.ToolCategory;
import edu.virginia.vcgr.genii.client.queue.JobTicket;
import edu.virginia.vcgr.genii.client.queue.QueueManipulator;
import edu.virginia.vcgr.genii.client.gpath.*;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.cmd.tools.Option;

public class QSubTool extends BaseGridTool
{
	static private final String _DESCRIPTION =
		"edu/virginia/vcgr/genii/client/cmd/tools/description/dqsub";
	static private final String _USAGE =
		"edu/virginia/vcgr/genii/client/cmd/tools/usage/uqsub";
	static final private String _MANPAGE =
		"edu/virginia/vcgr/genii/client/cmd/tools/man/qsub";
	private int _priority = 0;
	
	public QSubTool()
	{
		super(new FileResource(_DESCRIPTION), new FileResource(_USAGE), false, ToolCategory.EXECUTION);
		addManPage(new FileResource(_MANPAGE));
	}
	
	@Option({"priority"})
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
		GeniiPath gPath = new GeniiPath(getArgument(0));
		if(gPath.pathType() != GeniiPathType.Grid)
			throw new InvalidToolUsageException("<queue-path> must be a grid path");
		String queuePath = gPath.path();
		QueueManipulator manipulator = new QueueManipulator(queuePath);
		gPath = new GeniiPath(getArgument(1));
		JobTicket ticket;
		if(gPath.pathType() == GeniiPathType.Local)
		{
			String jsdlFile = new GeniiPath(getArgument(1)).path();
			ticket = manipulator.submit(new File(jsdlFile), _priority);
		}
		else		
		{
			ticket = manipulator.submit(gPath.openInputStream(), _priority);
		}
		
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