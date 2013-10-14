package edu.virginia.vcgr.genii.client.gui.browser.plugins;

/**
 * A common exception class that the plugin infrastructure uses to communicate exceptions.
 * 
 * @author mmm2a
 */
public class PluginException extends Exception
{
	static final long serialVersionUID = 0L;

	/**
	 * Create a new plugin exception.
	 * 
	 * @param msg
	 *            A message describing the exceptional condition.
	 */
	public PluginException(String msg)
	{
		super(msg);
	}

	/**
	 * Create a new plugin exception.
	 * 
	 * @param msg
	 *            A message describing the exceptional condition.
	 * @param cause
	 *            Another exception which caused this one to occur.
	 */
	public PluginException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}