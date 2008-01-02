package edu.virginia.vcgr.genii.client.utils.ui;

/**
 * A common exception class used the UI widgets.
 * 
 * @author mmm2a
 */
public class UIException extends Exception
{
	static final long serialVersionUID = 0L;
	
	/**
	 * Create a new UIException instance.
	 * 
	 * @param message The message that describes this exception.
	 */
	public UIException(String message)
	{
		super(message);
	}
	
	/**
	 * Create a new UIException instance.
	 * 
	 * @param message The message that describes this exception.
	 * @param cause The exception that lead to this UIException being
	 * thrown.
	 */
	public UIException(String message, Throwable cause)
	{
		super(message, cause);
	}
}