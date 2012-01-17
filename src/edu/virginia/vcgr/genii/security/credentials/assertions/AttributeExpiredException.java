package edu.virginia.vcgr.genii.security.credentials.assertions;

public class AttributeExpiredException extends AttributeInvalidException
{

	static public final long serialVersionUID = 0L;

	/**
	 * Constructs a AttributeExpiredException with no detail message.
	 */
	public AttributeExpiredException()
	{
		super();
	}

	/**
	 * Constructs a AttributeExpiredException with the specified detail message.
	 * A detail message is a String that describes this particular exception.
	 * 
	 * @param msg
	 *            the detail message.
	 */
	public AttributeExpiredException(String msg)
	{
		super(msg);
	}

	/**
	 * Creates a <code>AttributeExpiredException</code> with the specified
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
	public AttributeExpiredException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * Creates a <code>AttributeExpiredException</code> with the specified
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
	public AttributeExpiredException(Throwable cause)
	{
		super(cause);
	}

}
