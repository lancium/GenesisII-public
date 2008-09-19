package edu.virginia.vcgr.genii.client.cmd.tools;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.gui.GuiUtils;
import edu.virginia.vcgr.genii.client.gui.browser.BrowserDialog;
import edu.virginia.vcgr.genii.client.gui.browser.plugins.PluginManager;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.enhancedrns.EnhancedRNSPortType;

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
		EndpointReferenceType epr = RNSPath.getCurrent().getEndpoint();
		long start = System.currentTimeMillis();
		for (int lcv = 0; lcv < 10; lcv++)
		{
			ClientUtils.createProxy(EnhancedRNSPortType.class, epr);
		}
		System.err.println("Took " + (System.currentTimeMillis() - start) + " millis.");
		if (true)
			return 0;
		
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
