package edu.virginia.vcgr.genii.client.utils.ui;

/**
 * @author mmm2a
 *
 * This interface is the base interface of all UI elements.  The reason that 
 * it has the displayError method included is that sometimes when errors are
 * displayed (particularly for GUI dialogs) they are displayed relative to
 * another display component.
 */
public interface UIElement
{
	/**
	 * Display an error message in whatever way is appropriate for the kind
	 * of UI being implemented.
	 * 
	 * @param message The error message to display.
	 * 
	 * @throws UIException
	 */
	public void displayError(String message) throws UIException;
}