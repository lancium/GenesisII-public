package edu.virginia.vcgr.genii.client.gui.browser.plugins;

import java.util.Comparator;

/**
 * The tab plugin descriptor is a wrapper which holds information
 * describing a tab plugin.
 * 
 * @author mmm2a
 */
public class TabPluginDescriptor 
	extends PluginDescriptor<ITabPlugin>
{
	static private Comparator<TabPluginDescriptor> _sorter =
		new PrioritySorter();
	
	private String _tabName;
	private int _priority;
	
	/**
	 * Create a new tab plugin descriptor.
	 * 
	 * @param pluginName The common name of this plugin.
	 * @param plugin The implementing plugin instance.
	 * @param tabName The name that the tab should have.
	 * @param priority The priority that the tab should have.  Priorities
	 * affect the order in which tabs are displayed and go from lower
	 * numbers to higher numbers.
	 */
	public TabPluginDescriptor(
		String pluginName, ITabPlugin plugin,
		String tabName, int priority)
	{
		super(pluginName, plugin);
		
		_tabName = tabName;
		_priority = priority;
	}
	
	/**
	 * Retrieve the tab name for this plugin.
	 * 
	 * @return The tab's name.
	 */
	public String getTabName()
	{
		return _tabName;
	}
	
	/**
	 * Retrieve the tab priority for this plugin.
	 * 
	 * @return The tab priority.
	 */
	public int getPriority()
	{
		return _priority;
	}
	
	/**
	 * Return a comparator that can be used to sort tab descriptors based on
	 * priority.
	 * 
	 * @return The tab priority comparator.
	 */
	static public Comparator<TabPluginDescriptor> getPriorityComparator()
	{
		return _sorter;
	}
	
	/**
	 * This internal class implements the Comparator interface so that
	 * tab plugins can easily be sorted by priority.
	 * 
	 * @author mmm2a
	 */
	static private class PrioritySorter 
		implements Comparator<TabPluginDescriptor>
	{
		@Override
		public int compare(TabPluginDescriptor o1, TabPluginDescriptor o2)
		{
			return o1._priority - o2._priority;
		}	
	}
}