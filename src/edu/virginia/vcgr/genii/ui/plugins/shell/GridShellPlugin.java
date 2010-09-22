package edu.virginia.vcgr.genii.ui.plugins.shell;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Collection;

import javax.swing.JFrame;

import org.morgan.utils.gui.GUIUtils;

import edu.virginia.vcgr.genii.ui.UIContext;
import edu.virginia.vcgr.genii.ui.plugins.AbstractUITopMenuPlugin;
import edu.virginia.vcgr.genii.ui.plugins.EndpointDescription;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginContext;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginException;
import edu.virginia.vcgr.genii.ui.shell.CommandPanel;

public class GridShellPlugin extends AbstractUITopMenuPlugin
{
	@Override
	public void performTopMenuAction(UIPluginContext context)
			throws UIPluginException
	{
		UIContext newContext = (UIContext)context.uiContext().clone();
		
		JFrame frame = new JFrame("Grid Shell");
		
		CommandPanel cPanel = new CommandPanel(newContext);
		
		Container container = frame.getContentPane();
		container.setLayout(new GridBagLayout());
		
		container.add(cPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			new Insets(5, 5, 5, 5), 5, 5));
		
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.pack();
		GUIUtils.centerWindow(frame);
		frame.setVisible(true);
		frame.toFront();
	}

	@Override
	public boolean isEnabled(
			Collection<EndpointDescription> selectedDescriptions)
	{
		return true;
	}
}