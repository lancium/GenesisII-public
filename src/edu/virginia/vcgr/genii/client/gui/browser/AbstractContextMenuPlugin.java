package edu.virginia.vcgr.genii.client.gui.browser;

public abstract class AbstractContextMenuPlugin extends AbstractPlugin implements
		IContextMenuPlugin
{
	private String _groupName;
	private String _menuLabel;
	
	protected AbstractContextMenuPlugin(
		String pluginName, String menuLabel, String groupName)
	{
		super(pluginName);
		
		_groupName = groupName;
		_menuLabel = menuLabel;
	}
	
	@Override
	public String getGroupName()
	{
		return _groupName;
	}

	@Override
	public String getMenuLabel()
	{
		return _menuLabel;
	}
}