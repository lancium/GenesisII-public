package edu.virginia.vcgr.genii.client.comm.attachments;

import java.rmi.RemoteException;

public class GeniiAttachmentException extends RemoteException
{
	static final long serialVersionUID = 0L;
	
	public GeniiAttachmentException(String msg)
	{
		super(msg);
	}
	
	public GeniiAttachmentException(Throwable cause)
	{
		super(cause.getLocalizedMessage(), cause);
	}
	
	public GeniiAttachmentException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}