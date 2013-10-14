package edu.virginia.vcgr.genii.network;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Some tools for working with the host's networking configuration.
 */
public class NetworkConfigTools
{
	static private Log _logger = LogFactory.getLog(NetworkConfigTools.class);

	public static Collection<String> getIPAddresses()
	{
		HashSet<String> ips = new HashSet<String>();
		StringBuilder log = new StringBuilder();
		try {
			Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
			while (e.hasMoreElements()) {
				NetworkInterface n = (NetworkInterface) e.nextElement();
				Enumeration<InetAddress> ee = n.getInetAddresses();
				while (ee.hasMoreElements()) {
					InetAddress i = (InetAddress) ee.nextElement();
					ips.add(i.getHostAddress());
					if (_logger.isDebugEnabled())
						log.append(i.getHostAddress() + " ");
				}
			}
		} catch (Throwable t) {
			String msg = "failed to query IP addresses for the host: " + t.getMessage();
			log.append(msg + "\n");
			_logger.error(msg);
		}
		if (_logger.isDebugEnabled())
			_logger.debug("IP addresses for server: " + log);
		return ips;
	}

}
