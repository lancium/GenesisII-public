package edu.virginia.vcgr.genii.client.cmd.tools;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.gui.GuiUtils;
import edu.virginia.vcgr.genii.client.gui.browser.BrowserDialog;
import edu.virginia.vcgr.genii.client.gui.browser.plugins.PluginManager;
import edu.virginia.vcgr.genii.client.io.FileResource;

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
		PluginManager pluginManager = PluginManager.loadPlugins(new FileResource(
			"edu/virginia/vcgr/genii/client/gui/browser/browser-config.xml"));
		BrowserDialog dialog = new BrowserDialog(pluginManager);
		
		GuiUtils.centerComponent(dialog);
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
