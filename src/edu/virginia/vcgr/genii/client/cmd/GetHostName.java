package edu.virginia.vcgr.genii.client.cmd;

import java.net.InetAddress;
import java.net.SocketException;

import edu.virginia.vcgr.genii.client.configuration.Hostname;

public class GetHostName
{
	static private final String _USAGE = "Usage: GetHostName [--ip] [--not-fully-qualified]";
	static private final String _IP_ARG = "--ip";
	static private final String _NOT_FULLY_QUALIFIED = "--not-fully-qualified";

	static private void printUsage()
	{
		System.out.print(_USAGE + "\n");
	}
	
	static private InetAddress getMostGlobal() throws SocketException
	{
		try
		{
			InetAddress addr = Hostname.getMostGlobal();
			return addr;
		}
		catch(Throwable t)
		{
			return null;
		}
	}
	
	static public String getHostNameIP() throws SocketException
	{
		InetAddress addr = getMostGlobal();
		if (addr == null)
			return null;
		return addr.getHostAddress();
	}
	
	static public String getHostName() throws SocketException
	{
		InetAddress addr = getMostGlobal();
		if (addr == null)
			return null;
		return addr.getCanonicalHostName();
	}
	
	static public void main(String [] args) throws SocketException
	{
		boolean getIP = false;
		boolean fullyQualified = true;

		for (String arg : args)
		{
			if (arg.equals(_IP_ARG))
				getIP = true;
			else if (arg.equals(_NOT_FULLY_QUALIFIED))
				fullyQualified = false;
			else
			{
				printUsage();
				System.exit(1);
			}
		}

		if (getIP)
			System.out.print(getHostNameIP() + "\n");
		else
		{
			String hostname = getHostName();
			if (!fullyQualified)
			{
				int index = hostname.indexOf('.');
				if (index > 0)
					hostname = hostname.substring(0, index);
			}
			System.out.print(hostname + "\n");
		}
	}
}
