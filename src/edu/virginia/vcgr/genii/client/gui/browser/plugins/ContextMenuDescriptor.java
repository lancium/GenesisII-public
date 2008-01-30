package edu.virginia.vcgr.genii.client.gui.browser.plugins;

/**
 * A context menu descriptor is a class that complete describes a registered
 * context plugin.  This is a plugin which acts in a menu that can be popped
 * up by the user by right-clicking on an element in the RNS tree.
 * 
 * @author mmm2a
 */
public class ContextMenuDescriptor extends PluginDescriptor<IMenuPlugin>
{
	private String _menuLabel;
	private String _menuGroup;
	
	/**
	 * Create a new context menu descriptor.
	 * 
	 * @param pluginName The name of the plugin that we are describing.
	 * @param plugin The menu plugin that implements the functionallity
	 * of this menu plugin.
	 * @param menuLabel The label that the menu should have for this
	 * plugin.
	 * @param menuGroup A string name that helps to group plug-ins together
	 * in the context menu.
	 */
	public ContextMenuDescriptor(String pluginName,
		IMenuPlugin plugin, String menuLabel, String menuGroup)
	{
		super(pluginName, plugin);
		
		_menuLabel = menuLabel;
		_menuGroup = menuGroup;
	}
	
	/**
	 * Simple menu label accessor.
	 * 
	 * @return The menu label.
	 */
	public String getMenuLabel()
	{
		return _menuLabel;
	}
	
	/**
	 * Simple menu group accessor.
	 * 
	 * @return The menu group name.
	 */
	public String getMenuGroup()
	{
		return _menuGroup;
	}
}
