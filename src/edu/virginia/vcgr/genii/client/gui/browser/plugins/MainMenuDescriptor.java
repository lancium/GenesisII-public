package edu.virginia.vcgr.genii.client.gui.browser.plugins;

public class MainMenuDescriptor extends ContextMenuDescriptor
{
	private String _menuName;
	
	public MainMenuDescriptor(String pluginName, IMenuPlugin plugin,
		String menuName, String menuLabel, String menuGroup)
	{
		super(pluginName, plugin, menuLabel, menuGroup);
		
		_menuName = menuName;
	}
	
	public String getMenuName()
	{
		return _menuName;
	}
}
