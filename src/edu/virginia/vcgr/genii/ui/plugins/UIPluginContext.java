package edu.virginia.vcgr.genii.ui.plugins;

import javax.swing.JComponent;

import edu.virginia.vcgr.genii.ui.ApplicationContext;
import edu.virginia.vcgr.genii.ui.UIContext;

final public class UIPluginContext
{
	private EndpointRetriever _endpointRetriever;
	private ApplicationContext _applicationContext;
	private UIContext _uiContext;
	private JComponent _ownerComponent;
	
	public UIPluginContext(ApplicationContext applicationContext,
		UIContext uiContext, JComponent ownerComponent,
		EndpointRetriever endpointRetriever)
	{
		_applicationContext = applicationContext;
		_uiContext = uiContext;
		_ownerComponent = ownerComponent;
		_endpointRetriever = endpointRetriever;
	}
	
	final public ApplicationContext applicationContext()
	{
		return _applicationContext;
	}
	
	final public UIContext uiContext()
	{
		return _uiContext;
	}
	
	final public JComponent ownerComponent()
	{
		return _ownerComponent;
	}
	
	final public EndpointRetriever endpointRetriever()
	{
		return _endpointRetriever;
	}
}