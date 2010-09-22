package edu.virginia.vcgr.genii.ui.plugins;

import java.util.Collection;

import javax.swing.AbstractAction;

public abstract class UIMenuPluginAction<Type extends UIMenuPlugin> extends AbstractAction
{
	static final long serialVersionUID = 0l;
		
	protected UIPluginContext _context;
	protected Type _plugin;
	
	UIMenuPluginAction(Type plugin, String name, UIPluginContext context) 
	{
		super(name);
		
		_plugin = plugin;
		_context = context;
	}
	
	@SuppressWarnings("unchecked")
	final Class<Type> pluginClass()
	{
		return (Class<Type>)_plugin.getClass();
	}
	
	final public void updateStatus(Collection<EndpointDescription> descriptions)
	{
		setEnabled(_plugin.isEnabled(descriptions));
	}
}