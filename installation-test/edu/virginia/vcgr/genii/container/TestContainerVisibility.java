package edu.virginia.vcgr.genii.container;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.virginia.vcgr.genii.client.configuration.Hostname;

public class TestContainerVisibility
{
	static private Hostname _hostname;
	
	@BeforeClass
	static public void beforeClassSetup()
	{
		_hostname = Hostname.getLocalHostname();
	}
	
	@Test
	public void testAddressQuality() throws UnknownHostException
	{
		InetAddress address = _hostname.getAddress();
		
		Assert.assertTrue("Hostname is IPv4",
			address instanceof Inet4Address);
		
		Assert.assertFalse("Is not wildecard address",
			address.isAnyLocalAddress());
		Assert.assertFalse("Is not loopback address",
			address.isLoopbackAddress());
		Assert.assertFalse("Is not link local address",
			address.isLinkLocalAddress());
		Assert.assertFalse("Is not site local address",
			address.isSiteLocalAddress());
		Assert.assertFalse("Is not multicast address",
			address.isMulticastAddress());
	}
	
	@Test(timeout = 8000L)
	public void testAddressSelfVisibility() throws IOException
	{
		InetAddress address = _hostname.getAddress();
		
		Assert.assertTrue("Is reachable", address.isReachable(7000));
	}
}