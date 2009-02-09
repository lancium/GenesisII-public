/*
 * Copyright 2006 University of Virginia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package edu.virginia.vcgr.genii.client.configuration;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.configuration.XMLConfiguration;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;

public class Hostname
{
	static private final String _EXTERNAL_HOSTNAME_OVERRIDE_PROPERTY =
		"edu.virginia.vcgr.genii.container.external-hostname-override";
	static private final String _EPR_ADDRESSING_MODE_PROPERTY =
		"edu.virginia.vcgr.genii.container.epr-addressing-mode";
	static private final String _EPR_ADDRESSING_MODE_DEFAULT_VALUE = "auto";
	
	static private Log _logger = LogFactory.getLog(Hostname.class);
	
	private InetAddress _address = null;
	private String _externalName;
	
	static private String getGlobalProperty(String propertyName)
	{
		try
		{
			XMLConfiguration conf =
				ConfigurationManager.getCurrentConfiguration().getRoleSpecificConfiguration();
			if (conf == null)
				return null;
			
			Properties props = (Properties)conf.retrieveSection(
				GenesisIIConstants.GLOBAL_PROPERTY_SECTION_NAME);
			
			return props.getProperty(propertyName);
		}
		catch (ConfigurationException ce)
		{
			_logger.debug(ce);
			return null;
		}
	}
	
	static private String getHostnameOverride()
	{
		return getGlobalProperty(_EXTERNAL_HOSTNAME_OVERRIDE_PROPERTY);
	}
	
	static private String getEPRAddressingMode()
	{
		return getGlobalProperty(_EPR_ADDRESSING_MODE_PROPERTY);
	}
	
	static private InetAddress getMostGlobal(InetAddress []addrs)
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
	
	static private String formString(InetAddress addr)
	{
		String mode = getEPRAddressingMode();
		if (mode == null)
			mode = _EPR_ADDRESSING_MODE_DEFAULT_VALUE;
		
		if (mode.equals("ip"))
			return addr.getHostAddress();
		else if (mode.equals("dns"))
			return addr.getCanonicalHostName();
		
		String dns = addr.getCanonicalHostName();
		if (dns.indexOf('.') >= 0)
			return dns;
		
		return addr.getHostAddress();
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

	private Hostname(String givenAddress)
		throws UnknownHostException, SocketException
	{
		InetAddress []addrs = InetAddress.getAllByName(givenAddress);
		InetAddress addr = getMostGlobal(addrs);
		if (addr == null)
			throw new UnknownHostException(givenAddress);
		
		if (addr.isLoopbackAddress())
		{
			_externalName = getHostnameOverride();
			if (_externalName != null)
				return;

			_address = getMostGlobal();
			_externalName = formString(_address);
		} else
		{
			_externalName = givenAddress;
//			_externalName = formString(addr);
		}
	}
	
	private Hostname() throws SocketException
	{
		_externalName = getHostnameOverride();
		if (_externalName != null)
			return;

		_address = getMostGlobal();
		_externalName = formString(_address);
	}
	
	public String toString()
	{
		return _externalName;
	}
	
	public String toShortString()
	{
		String ret = toString();
		
		int index = ret.indexOf('.');
		if (index > 0)
			ret = ret.substring(0, index);
		
		return ret;
	}
	
	synchronized public InetAddress getAddress()
		throws UnknownHostException
	{
		if (_address == null)
			_address = InetAddress.getByName(_externalName);
		
		return _address;
	}
	
	static private Hostname _localHost = null;
	synchronized static public Hostname getLocalHostname()
	{
		try
		{
			if (_localHost == null)
				_localHost = new Hostname();
			
			return _localHost;
		}
		catch (SocketException se)
		{
			_logger.error(se);
			throw new RuntimeException("Unable to determine local IP addr.", se);
		}
	}
	
	static public Hostname lookupHostname(String givenHostname)
		throws UnknownHostException
	{
		try
		{
			return new Hostname(givenHostname);
		}
		catch (SocketException se)
		{
			throw new UnknownHostException(givenHostname);
		}
	}
	
	static private Pattern _URL_PATTERN = Pattern.compile(
		"([^:]+):\\/\\/([^:\\/]+)(.*)");
	
	static public String normalizeURL(String url) throws UnknownHostException
	{
		Matcher urlMatcher = _URL_PATTERN.matcher(url);
		if (!urlMatcher.matches())
		{
			/* This doesn't allow for URI's in the name
			throw new IllegalArgumentException(
				"url \"" + url + "\" does not appear valid.");
			*/
			return url;
		}
		
		try
		{
			String proto = urlMatcher.group(1);
			String host = (new Hostname(urlMatcher.group(2))).toString();
			String rest = urlMatcher.group(3);
			
			return proto + "://" + host + rest;
		}
		catch (SocketException se)
		{
			_logger.debug(se);
			throw new UnknownHostException(urlMatcher.group(2));
		}
	}
}
