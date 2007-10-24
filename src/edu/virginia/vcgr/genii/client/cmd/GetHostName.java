package edu.virginia.vcgr.genii.client.cmd;

import java.net.InetAddress;
import java.net.SocketException;

import edu.virginia.vcgr.genii.client.configuration.Hostname;

public class GetHostName
{
	static private final String _USAGE = "Usage: GetHostName [--ip]";
	static private final String _IP_ARG = "--ip";

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
		if (args.length > 1 || (args.length == 1 && !args[0].equals(_IP_ARG)))
		{
			printUsage();
			return;
		}
		if (args.length == 1)
			System.out.print(getHostNameIP() + "\n");
		else
			System.out.print(getHostName() + "\n");
	}
}