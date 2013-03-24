package edu.virginia.vcgr.genii.client.gui.browser.plugins;

/**
 * A PluginDescriptor is a simple descriptor object which describes the content about a plugin which
 * is the same for all plugin types.
 * 
 * @author mmm2a
 * 
 * @param <Type>
 *            The type of plugin that you are describing.
 */
public class PluginDescriptor<Type extends IPlugin>
{
	private String _pluginName;
	private Type _plugin;

	/**
	 * Create a new PluginDescriptor
	 * 
	 * @param pluginName
	 *            The human readable name of this plugin.
	 * @param plugin
	 *            The actual plugin implementation.
	 */
	protected PluginDescriptor(String pluginName, Type plugin)
	{
		_pluginName = pluginName;
		_plugin = plugin;
	}

	/**
	 * A simple accessor to get the plugin name.
	 * 
	 * @return The plugin name.
	 */
	public String getPluginName()
	{
		return _pluginName;
	}

	/**
	 * A simple accessor to get the plugin implementation.
	 * 
	 * @return The plugin implementation object.
	 */
	public Type getPlugin()
	{
		return _plugin;
	}
}