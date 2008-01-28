package edu.virginia.vcgr.genii.client.gui.browser.plugins;

public class PluginDescriptor<Type extends IPlugin>
{
	private String _pluginName;
	private Type _plugin;
	
	protected PluginDescriptor(String pluginName, Type plugin)
	{
		_pluginName = pluginName;
		_plugin = plugin;
	}
	
	public String getPluginName()
	{
		return _pluginName;
	}
	
	public Type getPlugin()
	{
		return _plugin;
	}
}