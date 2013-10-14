package org.morgan.mnaming;

public class MNameNotBoundException extends MNamingException
{
	static final long serialVersionUID = 0L;

	public MNameNotBoundException(String name)
	{
		super(String.format("%s is not bound!", name));
	}
}
