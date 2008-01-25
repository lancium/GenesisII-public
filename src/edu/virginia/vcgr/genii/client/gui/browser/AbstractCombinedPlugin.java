package edu.virginia.vcgr.genii.client.gui.browser;

public abstract class AbstractCombinedPlugin extends AbstractMainMenuPlugin implements
		IMainMenuPlugin, IContextMenuPlugin, ITabPlugin
{
	private String _tabName;
	private int _priority;
	
	protected AbstractCombinedPlugin(
		String pluginName,
		String menuName, String menuLabel, String groupName,
		String tabName, int priority)
	{
		super(pluginName, menuName, menuLabel, groupName);
		
		_tabName = tabName;
		_priority = priority;
	}
	
	@Override
	public int getPriority()
	{
		return _priority;
	}

	@Override
	public String getTabLabel()
	{
		return _tabName;
	}
}