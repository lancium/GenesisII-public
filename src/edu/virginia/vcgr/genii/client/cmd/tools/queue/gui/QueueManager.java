package edu.virginia.vcgr.genii.client.cmd.tools.queue.gui;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.cmd.tools.BaseGridTool;
import edu.virginia.vcgr.genii.client.gui.GuiUtils;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;

public class QueueManager extends BaseGridTool
{
	static private final String _DESCRIPTION = 
		"Manage jobs in a queue from a graphical interface.";
	static private final String _USAGE =
		"qmgr <queue-path>";
	
	public QueueManager()
	{
		super(_DESCRIPTION, _USAGE, true);
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		RNSPath queue = RNSPath.getCurrent().lookup(
			getArgument(0), RNSPathQueryFlags.MUST_EXIST);
		TypeInformation typeInfo = new TypeInformation(queue.getEndpoint());
		if (!typeInfo.isQueue())
			throw new InvalidToolUsageException(String.format(
				"Path \"%s\" does not refer to a grid queue.",
				getArgument(0)));
		
		QueueManagerDialog dialog = new QueueManagerDialog(queue.getEndpoint());
		dialog.pack();
		GuiUtils.centerComponent(dialog);
		dialog.setVisible(true);
		
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 1)
			throw new InvalidToolUsageException(
				"Command must contain the path to a grid queue.");
	}
}