package edu.virginia.vcgr.genii.ui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPopupMenu;

import edu.virginia.vcgr.genii.ui.plugins.UIPlugins;

public class RNSTreePopupListener extends MouseAdapter
{
	private UIPlugins _plugins;
	
	private void doRightClick(MouseEvent e)
	{
		JPopupMenu pMenu = _plugins.createPopupMenu();
		if (pMenu != null)
			pMenu.show(e.getComponent(), e.getX(), e.getY());
	}
	
	public RNSTreePopupListener(UIPlugins plugins)
	{
		_plugins = plugins;
	}
	
	@Override
	public void mouseClicked(MouseEvent e)
	{
		if (e.isPopupTrigger())
			doRightClick(e);
	}

	@Override
	public void mousePressed(MouseEvent e)
	{
		if (e.isPopupTrigger())
			doRightClick(e);
	}

	@Override
	public void mouseReleased(MouseEvent e)
	{
		if (e.isPopupTrigger())
			doRightClick(e);
	}
}