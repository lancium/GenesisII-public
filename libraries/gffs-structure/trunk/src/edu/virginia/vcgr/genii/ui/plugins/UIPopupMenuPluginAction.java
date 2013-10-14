package edu.virginia.vcgr.genii.ui.plugins;

import java.awt.event.ActionEvent;

import edu.virginia.vcgr.genii.ui.errors.ErrorHandler;

public class UIPopupMenuPluginAction extends UIMenuPluginAction<UIPopupMenuPlugin>
{
	static final long serialVersionUID = 0l;

	UIPopupMenuPluginAction(UIPopupMenuPlugin plugin, String name, UIPluginContext context)
	{
		super(plugin, name, context);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		try {
			_plugin.performPopupMenuAction(_context);
		} catch (Throwable cause) {
			ErrorHandler.handleError(_context.uiContext(), _context.ownerComponent(), cause);
		}
	}
}