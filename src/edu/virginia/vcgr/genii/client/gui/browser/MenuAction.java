package edu.virginia.vcgr.genii.client.gui.browser;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.gui.browser.plugins.ContextMenuDescriptor;
import edu.virginia.vcgr.genii.client.gui.browser.plugins.IMenuPlugin;
import edu.virginia.vcgr.genii.client.gui.browser.plugins.PluginException;
import edu.virginia.vcgr.genii.client.gui.browser.plugins.PluginStatus;
import edu.virginia.vcgr.genii.client.rns.RNSPath;

public class MenuAction extends AbstractAction
	implements TreeSelectionListener
{
	static final long serialVersionUID = 0L;

	static private Log _logger = LogFactory.getLog(MenuAction.class);
	
	private ISelectionCallback _selectionCallback;
	private BrowserDialog _ownerDialog;
	private ContextMenuDescriptor _descriptor;
	
	public MenuAction(BrowserDialog ownerDialog,
		ISelectionCallback selectionCallback,
		ContextMenuDescriptor descriptor)
	{
		super(descriptor.getMenuLabel());
	
		if (selectionCallback == null)
			throw new IllegalArgumentException("selectionCallback cannot be null.");
		
		_descriptor = descriptor;
		_ownerDialog = ownerDialog;
		_selectionCallback = selectionCallback;
		
		valueChanged(null);
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
	{
		RNSPath []selectedResources = _selectionCallback.getSelectedPaths();
		IMenuPlugin plugin = _descriptor.getPlugin();
		
		try
		{
			if (plugin.getStatus(selectedResources) == PluginStatus.ACTIVTE)
				plugin.performAction(selectedResources, _ownerDialog,
					_ownerDialog.getActionContext());
		}
		catch (PluginException pe)
		{
			_logger.error("Plugin threw exception.", pe);
		}
	}
	
	@Override
	public void valueChanged(TreeSelectionEvent e)
	{
		try
		{
			setEnabled(_descriptor.getPlugin().getStatus(
				_selectionCallback.getSelectedPaths()) == PluginStatus.ACTIVTE);
		}
		catch (PluginException pe)
		{
			_logger.error("Plugin threw exception.", pe);
			setEnabled(false);
		}
	}
}