package edu.virginia.vcgr.genii.client.cmd.tools;

import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JDialog;
import javax.swing.JScrollPane;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.gui.widgets.rns.RNSTree;

public class BrowserTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"Tests the RNS browser.";
	static final private String _USAGE =
		"browser";
	
	public BrowserTool()
	{
		super(_DESCRIPTION, _USAGE, false);
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		JDialog dialog = new JDialog();
		dialog.setTitle("RNS Browser");
		Container pane = dialog.getContentPane();
		JScrollPane jp = new JScrollPane(new RNSTree());
		

		Dimension size = new Dimension(250, 250);
		jp.setPreferredSize(size);
		
		pane.add(jp);
		dialog.pack();
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
