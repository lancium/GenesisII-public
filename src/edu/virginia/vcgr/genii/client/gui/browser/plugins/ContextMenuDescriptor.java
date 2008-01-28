package edu.virginia.vcgr.genii.client.gui.browser.plugins;

public class ContextMenuDescriptor extends PluginDescriptor<IMenuPlugin>
{
	private String _menuLabel;
	private String _menuGroup;
	
	public ContextMenuDescriptor(String pluginName,
		IMenuPlugin plugin, String menuLabel, String menuGroup)
	{
		super(pluginName, plugin);
		
		_menuLabel = menuLabel;
		_menuGroup = menuGroup;
	}
	
	public String getMenuLabel()
	{
		return _menuLabel;
	}
	
	public String getMenuGroup()
	{
		return _menuGroup;
	}
}
