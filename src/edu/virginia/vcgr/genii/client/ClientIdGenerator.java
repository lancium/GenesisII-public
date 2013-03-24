package edu.virginia.vcgr.genii.client;

import org.morgan.util.GUID;

public class ClientIdGenerator
{

	private static final String CLIENT_ID;
	static {
		CLIENT_ID = new GUID().toString();
	}

	public static String getClientId()
	{
		return CLIENT_ID;
	}
}
