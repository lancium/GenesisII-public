package edu.virginia.vcgr.genii.client.cmd.tools;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JDialog;
import javax.swing.JScrollPane;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.gui.GuiUtils;
import edu.virginia.vcgr.genii.client.gui.browser.RNSTree;

public class BrowserTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"Displays a file system browser with many interesting tools.";
	static final private String _USAGE =
		"browser";
	
	public BrowserTool()
	{
		super(_DESCRIPTION, _USAGE, true);
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		JDialog dialog = new JDialog();
		dialog.setTitle("Browser");
		dialog.getContentPane().setLayout(new GridBagLayout());
		dialog.getContentPane().add(new JScrollPane(new RNSTree()),
			new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(5, 5, 5, 5), 5, 5));
		dialog.pack();
		GuiUtils.centerComponent(dialog);
		dialog.setModal(true);
		dialog.setVisible(true);
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 0)
			throw new InvalidToolUsageException();
	}
}