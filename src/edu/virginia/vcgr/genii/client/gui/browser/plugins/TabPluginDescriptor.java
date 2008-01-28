package edu.virginia.vcgr.genii.client.gui.browser.plugins;

import java.util.Comparator;

public class TabPluginDescriptor 
	extends PluginDescriptor<ITabPlugin>
{
	static private Comparator<TabPluginDescriptor> _sorter =
		new PrioritySorter();
	
	private String _tabName;
	private int _priority;
	
	public TabPluginDescriptor(
		String pluginName, ITabPlugin plugin,
		String tabName, int priority)
	{
		super(pluginName, plugin);
		
		_tabName = tabName;
		_priority = priority;
	}
	
	public String getTabName()
	{
		return _tabName;
	}
	
	public int getPriority()
	{
		return _priority;
	}
	
	static public Comparator<TabPluginDescriptor> getPriorityComparator()
	{
		return _sorter;
	}
	
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