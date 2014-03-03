package edu.virginia.vcgr.appmgr.net;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;

public class Hostname {
	static private String _hostname = null;
	static private String _ipAddress = null;

	static private InetAddress getMostGlobal(Collection<InetAddress> addrs) {
		InetAddress[] ret = new InetAddress[5];
		Arrays.fill(ret, null);

		for (InetAddress addr : addrs) {
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
			if (ret[lcv] != null)
				return ret[lcv];

		throw new RuntimeException(
				"Unable to determine local host's network address.");
	}

	static private InetAddress getMostGlobal() {
		try {
			Collection<InetAddress> tmp = new ArrayList<InetAddress>();
			Enumeration<NetworkInterface> interfaces = NetworkInterface
					.getNetworkInterfaces();

			while (interfaces.hasMoreElements()) {
				NetworkInterface iface = interfaces.nextElement();
				Enumeration<InetAddress> addrs = iface.getInetAddresses();
				while (addrs.hasMoreElements())
					tmp.add(addrs.nextElement());
			}

			return getMostGlobal(tmp);
		} catch (SocketException se) {
			throw new RuntimeException(
					"Unable to determine local host's network address.", se);
		}
	}

	synchronized static public String getCurrentHostname() {
		if (_hostname == null) {
			InetAddress addr = getMostGlobal();
			_hostname = addr.getCanonicalHostName();
			_ipAddress = addr.getHostAddress();
		}

		return _hostname;
	}

	synchronized static public String getCurrentIPAddress() {
		if (_ipAddress == null) {
			InetAddress addr = getMostGlobal();
			_hostname = addr.getCanonicalHostName();
			_ipAddress = addr.getHostAddress();
		}

		return _ipAddress;
	}
}