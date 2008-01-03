package edu.virginia.vcgr.genii.client.utils.dialog;

import java.io.IOException;

import edu.virginia.vcgr.genii.client.io.FileResource;

/**
 * All dialog boxes (or widgets) implement this interface.
 * 
 * @author mmm2a
 */
public interface Widget
{
	/**
	 * Show the given message as an error in whatever way is appropriate for
	 * the gui system being used (text, graphic, etc.).
	 * 
	 * @param message The message to display.
	 */
	public void showErrorMessage(String message);
	
	/**
	 * Show detailed information about the exception indicated as an error
	 * box.
	 * 
	 * @param de The exception to display information about.
	 */
	public void showErrorMessage(DialogException de);
	
	/**
	 * Sets the detailed help message that get's displayed for this widget.
	 * This message gets displayed according to the rules of the widget
	 * system being used (for example, it may get displayed no matter
	 * what, or a user may have to click on a get help button or something).
	 * 
	 * @param detailedHelp The detailed help message to display.
	 */
	public void setDetailedHelp(String detailedHelp);
	
	/**
	 * Similar to the setDetailedHelp(String detailedHelp) method, this
	 * method sets a detailed help message to display, but the message itself
	 * in this case is retrieved from a class loader resource file.
	 * 
	 * @param detailedHelpResource The file resource to use to obtain the
	 * detailed help message.
	 * 
	 * @throws IOException
	 */
	public void setDetailedHelp(FileResource detailedHelpResource) 
		throws IOException;
	
	/**
	 * Set's the title for this widget/dialog.
	 * 
	 * @param title The title to give this widget or dialog.  This may or
	 * may not get displayed.
	 */
	public void setTitle(String title);
	
	/**
	 * Show and handle the widget.
	 */
	public void showWidget() throws DialogException;
}