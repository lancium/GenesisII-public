package edu.virginia.vcgr.genii.client.utils.ui;

/**
 * The widget interface for menu widgets.
 * 
 * @author mmm2a
 */
public interface UIMenu extends UIElement
{
	/**
	 * Choose a menu item from the stored menu.
	 * 
	 * @return The menu item (not key) chosen.
	 * 
	 * @throws UIException
	 */
	public Object choose() throws UIException;
}