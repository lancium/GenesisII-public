package org.morgan.mnaming;

public class MNameAlreadyBoundException extends MNamingException
{
	static final long serialVersionUID = 0L;

	public MNameAlreadyBoundException(String name)
	{
		super(String.format("%s is already bound!", name));
	}
}
