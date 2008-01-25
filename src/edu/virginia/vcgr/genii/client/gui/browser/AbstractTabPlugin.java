package edu.virginia.vcgr.genii.client.gui.browser;

public abstract class AbstractTabPlugin extends AbstractPlugin implements ITabPlugin
{
	private String _tabLabel;
	private int _priority;
	
	protected AbstractTabPlugin(String pluginName,
		String tabLabel, int priority)
	{
		super(pluginName);
		
		_tabLabel = tabLabel;
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
		return _tabLabel;
	}
}