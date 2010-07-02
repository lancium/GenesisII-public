package edu.virginia.vcgr.genii.client.cmd.tools.queue;

import java.util.Collection;
import java.util.List;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.cmd.tools.BaseGridTool;
import edu.virginia.vcgr.genii.client.queue.JobTicket;
import edu.virginia.vcgr.genii.client.queue.QueueManipulator;
import edu.virginia.vcgr.genii.client.gpath.*;


public class QQueryTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"Retrieves any error information associated with a job in a queue.";
	static final private String _USAGE =
		"qquery <queue-path> <ticket>";
	
	public QQueryTool()
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
		
		List<Collection<String>> errors = manipulator.queryErrorInformation(
			new JobTicket(getArgument(1)));
		
		for (int lcv = 0; lcv < errors.size(); lcv++)
		{
			Collection<String> texts = errors.get(lcv);
			if (texts != null && texts.size() > 0)
			{
				stdout.format("Attempt %d:\n\n", lcv);
				for (String str : texts)
				{
					stdout.format("%s\n\n", str);
				}
				stdout.println("*************************************\n");
			}
		}
		
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 2)
			throw new InvalidToolUsageException();
	}
}