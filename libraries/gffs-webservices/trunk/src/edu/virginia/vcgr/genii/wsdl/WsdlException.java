package edu.virginia.vcgr.genii.wsdl;

public class WsdlException extends Exception
{
	static final long serialVersionUID = 0L;

	static private final String _DEFAULT_MESSAGE = "Generic WSDL Exception.";

	public WsdlException()
	{
		super(_DEFAULT_MESSAGE);
	}

	public WsdlException(String msg)
	{
		super(msg);
	}

	public WsdlException(Throwable cause)
	{
		super(cause);
	}

	public WsdlException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}