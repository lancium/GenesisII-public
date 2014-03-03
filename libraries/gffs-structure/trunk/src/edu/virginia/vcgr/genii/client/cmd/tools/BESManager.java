package edu.virginia.vcgr.genii.client.cmd.tools;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.cmd.tools.besmgr.BESManagerDialog;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.gui.GuiUtils;
import edu.virginia.vcgr.genii.client.gui.bes.BESSelectorDialog;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;

public class BESManager extends BaseGridTool
{
	static final private String DESCRIPTION = "config/tooldocs/description/dbes-manager";
	static final private String USAGE = "config/tooldocs/usage/ubes-manager";
	static final private String _MANPAGE = "config/tooldocs/man/bes-manager";

	private EndpointReferenceType getLocalBESContainer()
	{
		return BESSelectorDialog.selectBESContainer(null);
	}

	public BESManager()
	{
		super(new LoadFileResource(DESCRIPTION), new LoadFileResource(USAGE), false, ToolCategory.ADMINISTRATION);
		addManPage(new LoadFileResource(_MANPAGE));
	}

	@Override
	protected int runCommand() throws Throwable
	{
		EndpointReferenceType target = null;

		if (numArguments() == 1) {
			RNSPath targetPath = lookup(new GeniiPath(getArgument(0)), RNSPathQueryFlags.MUST_EXIST);
			target = targetPath.getEndpoint();
		} else {
			target = getLocalBESContainer();
			if (target == null)
				return 0;
		}

		BESManagerDialog dialog = new BESManagerDialog(null, target);
		dialog.pack();
		GuiUtils.centerComponent(dialog);
		dialog.setVisible(true);

		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() > 1)
			throw new InvalidToolUsageException("Too many arguments given.");
	}
}