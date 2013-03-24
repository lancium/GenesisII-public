package edu.virginia.vcgr.genii.client.gui.browser.grid;

import edu.virginia.vcgr.genii.client.rns.RNSPath;

/**
 * This interface represents an object that is given to certain plug'ins (the ones that have to do
 * with menus) so that they can ask the main dialog to do certain things for them. Its essentially a
 * plugin context within which actions take place.
 * 
 * @author mmm2a
 */
public interface IActionContext
{
	/**
	 * This is a convenience method that a client can use to request that the main dialog perform a
	 * potentially long running action in the background. The dialog will launch this action in a
	 * background thread. It's up to the client code however to update the RNS Tree or other GUI
	 * components and on the correct event dispatch thread when the long running actions are taking
	 * place or done.
	 * 
	 * @param action
	 *            An object that can implement the long running action.
	 */
	public void performLongRunningAction(ILongRunningAction action);

	/**
	 * This method is called by the plug-in code when a portion of the RNS tree needs to be updated.
	 * Everything under the given path will be refreshed for the user.
	 * 
	 * @param subtreePath
	 *            The RNS path under which the refresh should happen.
	 */
	public void refreshSubTree(RNSPath subtreePath);

	/**
	 * This method is a convenience method that the plug-in can call to have an exception message
	 * displayed in a formatted exception dialog box.
	 * 
	 * @param msg
	 *            The error details to display.
	 */
	public void reportError(String msg);

	/**
	 * This method is a convenience method that the plug-in can call to have an exception message
	 * displayed in a formatted exception dialog box.
	 * 
	 * @param msg
	 *            The error details to display.
	 * @param cause
	 *            The exception that caused the error and which will be displayed in a text area
	 *            below the message label.
	 */
	public void reportError(String msg, Throwable cause);
}