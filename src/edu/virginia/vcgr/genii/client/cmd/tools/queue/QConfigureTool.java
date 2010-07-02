package edu.virginia.vcgr.genii.client.cmd.tools.queue;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.cmd.tools.BaseGridTool;
import edu.virginia.vcgr.genii.client.queue.QueueManipulator;
import edu.virginia.vcgr.genii.client.gpath.*;

public class QConfigureTool extends BaseGridTool
{
	static private final String _DESCRIPTION = 
		"Configures a given resource contained within a queue to have " +
		"a different number of slots then it currently does.";
	static private final String _USAGE =
		"qconfigure <queue-path> <resource-name> <num-slots>";
	
	public QConfigureTool()
	{
		super(_DESCRIPTION, _USAGE, false);
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		GeniiPath gPath = new GeniiPath(getArgument(0));
		if (gPath.pathType() != GeniiPathType.Grid)
			throw new InvalidToolUsageException("<queue-path> must be a grid path. ");
		QueueManipulator manipulator = new QueueManipulator(gPath.path());
		manipulator.configure(getArgument(1), Integer.parseInt(getArgument(2)));
		
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 3)
			throw new InvalidToolUsageException();
	}
}