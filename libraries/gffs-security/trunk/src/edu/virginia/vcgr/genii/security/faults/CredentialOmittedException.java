package edu.virginia.vcgr.genii.security.faults;

/**
 * this type of exception represents a failure in credential tracking when credential streamlining is enabled. the recipient of a batch of
 * credentials is claiming that they do not possess the required cached credentials in order to recognize the set found in a soap header as
 * completely valid. it is the recipient's responsibility to recreate the full credentials required and to re-attempt the request.
 */
public class CredentialOmittedException extends AttributeInvalidException
{
	static public final long serialVersionUID = 0L;

	/*
	 * hmmm: this class could use a support function for pulling the guids that failed out of the list. that's done in someplace else
	 * currently.
	 */

	public CredentialOmittedException()
	{
		super();
	}

	public CredentialOmittedException(String msg)
	{
		super(msg);
	}

	public CredentialOmittedException(String message, Throwable cause)
	{
		super(message, cause);
	}
}
