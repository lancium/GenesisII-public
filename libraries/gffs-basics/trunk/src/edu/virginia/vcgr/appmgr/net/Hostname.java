/*
 * Copyright 2006 University of Virginia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */
package edu.virginia.vcgr.appmgr.net;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Hostname
{
	static private Log _logger = LogFactory.getLog(Hostname.class);

	public static Pattern _URL_PATTERN = Pattern.compile("([^:]+):\\/\\/([^:\\/]+)(.*)");

	public static final String _EXTERNAL_HOSTNAME_OVERRIDE_PROPERTY = "edu.virginia.vcgr.genii.container.external-hostname-override";
	public static final String _EPR_ADDRESSING_MODE_PROPERTY = "edu.virginia.vcgr.genii.container.epr-addressing-mode";
	public static final String _EPR_ADDRESSING_MODE_DEFAULT_VALUE = "auto";

	// if we look up the hostname or main ip address of this machine, cache them so we don't pay that cost again.
	static private String _cachedHostname = null;
	static private String _cachedIPAddress = null;

	private String _externalName; // our hostname as it appears to the world, given EPR addressing scheme.
	private boolean _foundHostOkay = false; // tracks if the lookup succeeded.

	/*
	 * used only for local host to avoid repeated lookups. this differs from the _cachedHostName in that this string is formatted according to
	 * formString's requirements.
	 */
	private static String _staticVettedLocalHost = null;

	/**
	 * constructs a hostname based on our best guess for what this host is named. the valid() method reports whether this was successful or
	 * not.
	 */
	public Hostname()
	{
		this((String) null);
	}

	/**
	 * constructs a hostname based on the "givenAddress". if the "givenAddress" is null or blank, then the local host will be chosen instead.
	 * the valid() method reports whether this was successful or not.
	 */
	public Hostname(String givenAddress)
	{
		if ((givenAddress == null) || (givenAddress.length() == 0)) {
			if (_logger.isTraceEnabled())
				_logger.debug("into localhost case because given address is: " + givenAddress);

			// set the address to a known good name, in case we have to fall through.
			givenAddress = "localhost";

			// see if we already looked up the local host info.
			if (_staticVettedLocalHost != null) {
				_externalName = _staticVettedLocalHost;
				_foundHostOkay = true;
				return;
			}

			/*
			 * handle the null or blank hostname case, which indicates using the local host. this is a best-guess operation, where we may not
			 * always be able to answer with a hostname that the caller actually wants.
			 */
			_externalName = getHostnameOverride();
			if (_externalName != null) {
				_foundHostOkay = true;
			} else {
				InetAddress address = getMostGlobal();
				_externalName = formString(address);
				_foundHostOkay = true;
			}
			// set our persistent record for the localhost.
			_staticVettedLocalHost = _externalName;
			// done with localhost case, hopefully successfully.
			return;
		}

		if (_logger.isDebugEnabled())
			_logger.debug("going to look up address: " + givenAddress);

		// hmmm: here is a good place to have a DNS cache. just before looking up for real, is it in the cache? cache should maybe be lru
		// timeout.

		InetAddress addr = null;
		try {
			InetAddress[] addrs = InetAddress.getAllByName(givenAddress);
			addr = getMostGlobal(addrs);
			if (addr == null) {
				_logger.error("the host " + givenAddress + " could not be looked up successfully.");
				_foundHostOkay = false;
				return;
			}

			if (addr.isLoopbackAddress()) {
				_externalName = getHostnameOverride();
				if (_externalName != null) {
					// this is a success, it seems.
					_foundHostOkay = true;
					return;
				}

				InetAddress address = getMostGlobal();
				_externalName = formString(address);
			} else {
				_externalName = givenAddress;
			}

		} catch (UnknownHostException e) {
			_logger.error("the host " + givenAddress + " received an unknown host exception.", e);
			_foundHostOkay = false;
			return;
		}

		// if we are allowed to get here, it is assumed that things worked.
		_foundHostOkay = true;
	}

	/**
	 * reports on whether this hostname constructed properly based on the constructor parameters.
	 */
	public boolean valid()
	{
		return _foundHostOkay;
	}

	/**
	 * can be overridden by derived classes to provide a configuration file based version of the host name.
	 */
	public String getHostnameOverride()
	{
		return null;
	}

	/**
	 * can be overridden to load the epr addressing mode from a configuration source.
	 */
	public String getEPRAddressingMode()
	{
		return null;
	}

	private String formString(InetAddress addr)
	{
		String mode = getEPRAddressingMode();
		if (mode == null)
			mode = _EPR_ADDRESSING_MODE_DEFAULT_VALUE;

		if (mode.equals("ip")) {
			if (addr instanceof Inet6Address)
				return String.format("[%s]", addr.getHostAddress());
			else
				return addr.getHostAddress();
		} else if (mode.equals("dns"))
			return addr.getCanonicalHostName();

		String dns = addr.getCanonicalHostName();
		if (dns.indexOf('.') >= 0)
			return dns;

		return addr.getHostAddress();
	}

	public String getExternalName()
	{
		return _externalName;
	}

	@Override
	public String toString()
	{
		return getExternalName();
	}

	public String getShortExternalName()
	{
		String ret = getExternalName();

		int index = ret.indexOf('.');
		if (index > 0)
			ret = ret.substring(0, index);

		return ret;
	}

	/**
	 * looks up the internet address of the host that has been configured for this object.
	 */
	synchronized public InetAddress getAddress()
	{
		InetAddress address = null;
		try {
			address = InetAddress.getByName(_externalName);
		} catch (Throwable t) {
			_logger.error("failure to get address for hostname: " + _externalName);
		}
		return address;
	}

	synchronized static public String getCurrentHostname()
	{
		if (_cachedHostname == null) {
			InetAddress addr = getMostGlobal();
			_cachedHostname = addr.getCanonicalHostName();
			_cachedIPAddress = addr.getHostAddress();
		}

		return _cachedHostname;
	}

	synchronized static public String getCurrentIPAddress()
	{
		if (_cachedIPAddress == null) {
			InetAddress addr = getMostGlobal();
			_cachedHostname = addr.getCanonicalHostName();
			_cachedIPAddress = addr.getHostAddress();
		}

		return _cachedIPAddress;
	}

	static public String normalizeURL(String url) throws UnknownHostException
	{
		Matcher urlMatcher = Hostname._URL_PATTERN.matcher(url);
		if (!urlMatcher.matches()) {
			/*
			 * This doesn't allow for URI's in the name throw new IllegalArgumentException( "url \"" + url + "\" does not appear valid.");
			 */
			return url;
		}

		// try {
		String proto = urlMatcher.group(1);
		Hostname urf = new Hostname(urlMatcher.group(2));
		if (!urf.valid()) {
			throw new UnknownHostException(urlMatcher.group(2));
		}
		String host = urf.getExternalName();
		String rest = urlMatcher.group(3);

		return proto + "://" + host + rest;

		// } catch (SocketException se) {
		// if (Hostname._logger.isDebugEnabled())
		// Hostname._logger.debug(se);
		// throw new UnknownHostException(urlMatcher.group(2));
		// }
	}

	public static InetAddress getMostGlobal(InetAddress[] addrs)
	{
		// we create an array that can store our scoring system that is up to 5 elements (for scores from 0 to 4).
		InetAddress[] scoredList = new InetAddress[5];
		// fill the list with nulls to start.
		Arrays.fill(scoredList, null);

		// iterate across the addresses that we were given.
		for (InetAddress addr : addrs) {
			int score = 4;

			if (addr instanceof Inet6Address) {
				if (_logger.isTraceEnabled())
					_logger.debug(String.format("Skipping address \"%s\" because it's an IPv6 address.", addr));
				continue;
			} else {
				if (_logger.isTraceEnabled())
					_logger.debug("assessing address: " + addr);
			}

			if (addr.isAnyLocalAddress()) {
				continue;
			} else if (addr.isSiteLocalAddress()) {
				score = 3;
			} else if (addr.isLinkLocalAddress()) {
				score = 2;
			} else if (addr.isMulticastAddress()) {
				score = 1;
			} else if (addr.isLoopbackAddress()) {
				score = 0;
			}

			scoredList[score] = addr;
		}

		for (int lcv = 4; lcv >= 0; lcv--) {
			if (scoredList[lcv] != null)
				return scoredList[lcv];
		}

		throw new RuntimeException("Unable to determine local host's network address.");
	}

	public static InetAddress getMostGlobal(Collection<InetAddress> addrs)
	{
		// gnarly but simple copy of collection into array, since toArray does bizarre non-helpful things.
		InetAddress[] addressArray = new InetAddress[addrs.size()];
		int index = 0;
		for (InetAddress addr : addrs) {
			addressArray[index++] = addr;
		}
		return getMostGlobal(addressArray);
	}

	/**
	 * returns the most global hostname we know of for this host.
	 */
	public static InetAddress getMostGlobal()
	{
		try {
			Collection<InetAddress> tmp = new ArrayList<InetAddress>();
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface iface = interfaces.nextElement();
				Enumeration<InetAddress> addrs = iface.getInetAddresses();
				while (addrs.hasMoreElements()) {
					InetAddress addr = addrs.nextElement();
					tmp.add(addr);
				}
			}
			return getMostGlobal(tmp);
		} catch (SocketException se) {
			throw new RuntimeException("Unable to determine local host's network address.", se);
		}
	}
}