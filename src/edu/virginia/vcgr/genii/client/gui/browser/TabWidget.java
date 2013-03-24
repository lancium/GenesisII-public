package edu.virginia.vcgr.genii.client.gui.browser;

import java.awt.Dimension;
import java.util.TreeSet;

import javax.swing.JTabbedPane;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.gui.browser.plugins.ITabPlugin;
import edu.virginia.vcgr.genii.client.gui.browser.plugins.PluginException;
import edu.virginia.vcgr.genii.client.gui.browser.plugins.PluginManager;
import edu.virginia.vcgr.genii.client.gui.browser.plugins.PluginStatus;
import edu.virginia.vcgr.genii.client.gui.browser.plugins.TabPluginDescriptor;
import edu.virginia.vcgr.genii.client.rns.RNSPath;

/**
 * This is the main widget that handles the tabs to the right of the rns tree browser.
 * 
 * @author mmm2a
 */
public class TabWidget extends JTabbedPane implements TreeSelectionListener
{
	static final long serialVersionUID = 0L;

	static private Log _logger = LogFactory.getLog(TabWidget.class);

	private PluginManager _pluginManager;
	private ISelectionCallback _selectionCallback;

	/**
	 * Create a new tab widget.
	 * 
	 * @param selectionCallback
	 *            The selection callback to use to determine which rns paths are currently selected.
	 * @param pluginManager
	 *            The plugin manager from whence to get tabs.
	 */
	public TabWidget(ISelectionCallback selectionCallback, PluginManager pluginManager)
	{
		super();

		_pluginManager = pluginManager;
		_selectionCallback = selectionCallback;

		setTabs();

		Dimension d = new Dimension(600, 300);
		setPreferredSize(d);
		setMinimumSize(d);
	}

	/**
	 * This method is called every time we need to reset the tabs we are displaying (everytime the
	 * selection changes).
	 */
	private void setTabs()
	{
		removeAll();

		RNSPath[] paths = _selectionCallback.getSelectedPaths();

		/* Go through the list of tabs and find out which ones are active. */
		TreeSet<TabPluginDescriptor> tabs = _pluginManager.getTabs();
		for (TabPluginDescriptor tab : tabs) {
			ITabPlugin plugin = tab.getPlugin();

			try {
				if (plugin.getStatus(paths) == PluginStatus.ACTIVTE)
					add(tab.getTabName(), plugin.getComponent(paths));
			} catch (PluginException pe) {
				_logger.error("Plugin threw exception.", pe);
			}
		}
	}

	@Override
	public void valueChanged(TreeSelectionEvent e)
	{
		setTabs();
	}
}