package edu.virginia.vcgr.genii.ui.plugins;

import java.awt.event.ActionEvent;

import edu.virginia.vcgr.genii.ui.errors.ErrorHandler;

public class UITopMenuPluginAction extends UIMenuPluginAction<UITopMenuPlugin>
{
	static final long serialVersionUID = 0l;

	UITopMenuPluginAction(UITopMenuPlugin plugin, String name, UIPluginContext context)
	{
		super(plugin, name, context);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		try {
			_plugin.performTopMenuAction(_context);
		} catch (Throwable cause) {
			ErrorHandler.handleError(_context.uiContext(), _context.ownerComponent(), cause);
		}
	}
}
