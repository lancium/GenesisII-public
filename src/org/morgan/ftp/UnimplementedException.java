package org.morgan.ftp;

public class UnimplementedException extends FTPException
{
	static final long serialVersionUID = 0L;
	
	public UnimplementedException(String verb)
	{
		super(502, String.format("The verb \"%1$s\" is unimplemented.", verb));
	}
	
	public UnimplementedException(String verb, String parameterName)
	{
		super(504, String.format("The verb \"%1$s\" with parameter \"%2$s\" is unimplemented.",
			verb, parameterName));
	}
}