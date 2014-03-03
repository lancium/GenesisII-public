package edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe;

import java.rmi.RemoteException;

public class SubscribeException extends RemoteException
{
	static final long serialVersionUID = 0L;

	public SubscribeException(String msg)
	{
		super(msg);
	}

	public SubscribeException(String msg, Throwable cause)
	{
		super(msg, cause);
	}
}