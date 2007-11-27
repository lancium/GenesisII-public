package org.morgan.ftp;

public class ParameterFormatException extends FTPException
{
	static final long serialVersionUID = 0L;
	
	public ParameterFormatException(String parameter)
	{
		super(501, String.format("Format of parameter \"%1$s\" is invalid.",
			parameter));
	}
}