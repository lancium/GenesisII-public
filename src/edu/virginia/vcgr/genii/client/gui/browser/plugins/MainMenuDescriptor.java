package edu.virginia.vcgr.genii.client.gui.browser.plugins;

/**
 * The MainMenuDescriptor class is a descriptor wrapper that contains
 * information about plugins being used in the main, or top, menu bar.
 * 
 * @author mmm2a
 */
public class MainMenuDescriptor extends ContextMenuDescriptor
{
	private String _menuName;
	
	/**
	 * Create a new MainMenuDescrpitor.
	 * 
	 * @param pluginName The common or human readable name of the plugin.
	 * @param plugin The actual plugin implementation object.
	 * @param menuName The name of the menu to put this plug in to (this
	 * is the name displayed along the top bar).
	 * @param menuLabel The label that the menu item is to have inside
	 * the pull-down menu.
	 * @param menuGroup A human readable string which serves to group
	 * together menu items that are in the same group.
	 */
	public MainMenuDescriptor(String pluginName, IMenuPlugin plugin,
		String menuName, String menuLabel, String menuGroup)
	{
		super(pluginName, plugin, menuLabel, menuGroup);
		
		_menuName = menuName;
	}
	
	/**
	 * Retrieve the menu name that should be used for this menu plugin.
	 * 
	 * @return The menu name.
	 */
	public String getMenuName()
	{
		return _menuName;
	}
}
