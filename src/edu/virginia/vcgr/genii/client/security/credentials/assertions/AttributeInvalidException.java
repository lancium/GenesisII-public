package edu.virginia.vcgr.genii.client.security.credentials.assertions;

import java.security.GeneralSecurityException;

public class AttributeInvalidException extends GeneralSecurityException
{

	static public final long serialVersionUID = 0L;

	/**
	 * Constructs a AttributeInvalidException with no detail message.
	 */
	public AttributeInvalidException()
	{
		super();
	}

	/**
	 * Constructs a AttributeInvalidException with the specified detail message.
	 * A detail message is a String that describes this particular exception.
	 * 
	 * @param msg
	 *            the detail message.
	 */
	public AttributeInvalidException(String msg)
	{
		super(msg);
	}

	/**
	 * Creates a <code>AttributeInvalidException</code> with the specified
	 * detail message and cause.
	 * 
	 * @param message
	 *            the detail message (which is saved for later retrieval by the
	 *            {@link #getMessage()} method).
	 * @param cause
	 *            the cause (which is saved for later retrieval by the
	 *            {@link #getCause()} method). (A <tt>null</tt> value is
	 *            permitted, and indicates that the cause is nonexistent or
	 *            unknown.)
	 * @since 1.5
	 */
	public AttributeInvalidException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * Creates a <code>AttributeInvalidException</code> with the specified
	 * cause and a detail message of
	 * <tt>(cause==null ? null : cause.toString())</tt> (which typically
	 * contains the class and detail message of <tt>cause</tt>).
	 * 
	 * @param cause
	 *            the cause (which is saved for later retrieval by the
	 *            {@link #getCause()} method). (A <tt>null</tt> value is
	 *            permitted, and indicates that the cause is nonexistent or
	 *            unknown.)
	 * @since 1.5
	 */
	public AttributeInvalidException(Throwable cause)
	{
		super(cause);
	}

}
