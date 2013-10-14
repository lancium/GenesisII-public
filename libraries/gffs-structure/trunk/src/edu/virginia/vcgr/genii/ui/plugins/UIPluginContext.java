package edu.virginia.vcgr.genii.ui.plugins;

import javax.swing.JComponent;

import edu.virginia.vcgr.genii.ui.ApplicationContext;
import edu.virginia.vcgr.genii.ui.UIContext;

final public class UIPluginContext
{
	private EndpointRetriever _endpointRetriever;
	private UIContext _uiContext;
	private JComponent _ownerComponent;

	public UIPluginContext(UIContext uiContext, JComponent ownerComponent, EndpointRetriever endpointRetriever)
	{
		_uiContext = uiContext;
		_ownerComponent = ownerComponent;
		_endpointRetriever = endpointRetriever;
	}

	final public ApplicationContext applicationContext()
	{
		return _uiContext.applicationContext();
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