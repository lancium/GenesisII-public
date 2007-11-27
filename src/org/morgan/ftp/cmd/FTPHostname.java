package org.morgan.ftp.cmd;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

public class FTPHostname
{
	static final public String HOSTNAME_OVERRIDE_PROPERTY = "org.morgan.net.Hostname.hostname-override";
	
	static public InetAddress getMostGlobal(InetAddress []addrs)
	{
		InetAddress []ret = new InetAddress[5];
		for (int lcv = 0; lcv < ret.length; lcv++)
			ret[lcv] = null;
		
		for (InetAddress addr : addrs)
		{
			int score = 4;
			
			if (addr.isAnyLocalAddress())
				continue;
			else if (addr.isSiteLocalAddress())
				score = 3;
			else if (addr.isLinkLocalAddress())
				score = 2;
			else if (addr.isMulticastAddress())
				score = 1;
			else if (addr.isLoopbackAddress())
				score = 0;
			
			ret[score] = addr;
		}
		
		for (int lcv = 4; lcv >= 0; lcv--)
		{
			if (ret[lcv] != null)
				return ret[lcv];
		}
		
		return null;
	}
	
	static public InetAddress getMostGlobal() throws SocketException
	{
		ArrayList<InetAddress> tmp = new ArrayList<InetAddress>();
		Enumeration<NetworkInterface> interfaces =
			NetworkInterface.getNetworkInterfaces();
		
		while (interfaces.hasMoreElements())
		{
			NetworkInterface iface = interfaces.nextElement();
			Enumeration<InetAddress> addrs = iface.getInetAddresses();
			while (addrs.hasMoreElements())
			{
				tmp.add(addrs.nextElement());
			}
		}
		
		InetAddress []addrs = new InetAddress[tmp.size()];
		tmp.toArray(addrs);
		return getMostGlobal(addrs);
	}
	
	static public enum HostFormats
	{
		IP_ADDR(),
		DNS_NAME()
	}
	
	static public String format(InetAddress addr, HostFormats format)
	{
		if (format.equals(HostFormats.IP_ADDR))
		{
			return addr.getHostAddress();
		} else
		{
			return addr.getCanonicalHostName();
		}
	}
	
	static public String getLocalHost(HostFormats format)
		throws SocketException
	{
		String value = System.getProperty(HOSTNAME_OVERRIDE_PROPERTY);
		if (value != null)
			return value;
		
		return format(getMostGlobal(), format);
	}
}