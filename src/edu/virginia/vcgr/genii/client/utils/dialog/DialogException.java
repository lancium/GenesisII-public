package edu.virginia.vcgr.genii.client.utils.dialog;

/**
 * A common base class for all exceptions thrown by the dialog classes.
 * 
 * @author mmm2a
 */
public class DialogException extends Exception
{
	static final long serialVersionUID = 0L;
	
	/**
	 * Create a new dialog exception with the given message as it's detail.
	 * 
	 * @param message The message to use for this exception's detail.
	 */
	public DialogException(String message)
	{
		super(message);
	}
	
	/**
	 * Create a new dialog exception with the given message as it's detail.
	 * 
	 * @param message The message to use for this exception's detail.
	 * @param cause Another exception which caused this exception to occur.
	 */
	public DialogException(String message, Throwable cause)
	{
		super(message, cause);
	}
}