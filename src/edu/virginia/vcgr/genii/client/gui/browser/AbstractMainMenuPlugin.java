package edu.virginia.vcgr.genii.client.gui.browser;

public abstract class AbstractMainMenuPlugin extends AbstractContextMenuPlugin implements
		IMainMenuPlugin
{
	private String _menuName;
	
	protected AbstractMainMenuPlugin(String pluginName,
		String menuName, String menuLabel, String groupName)
	{
		super(pluginName, menuLabel, groupName);
		
		_menuName = menuName;
	}
	
	@Override
	public String getMenuName()
	{
		return _menuName;
	}
}