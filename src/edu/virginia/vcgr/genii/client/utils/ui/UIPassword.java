package edu.virginia.vcgr.genii.client.utils.ui;

/**
 * An interface for a widget that gets a password (doesn't echo the 
 * characters).
 * 
 * @author mmm2a
 */
public interface UIPassword extends UIElement
{
	/**
	 * Get the password in question without displaying what the user types in.
	 * 
	 * @param header An optional header to print before the password prompt.
	 * @param question The password prompt to display to the user.
	 * @return The password that the user typed in.  This value MIGHT be empty for
	 * an empty password, but will not be NULL.
	 * 
	 * @throws UIException
	 */
	public char[] getPassword(String header, String question) throws UIException;
}