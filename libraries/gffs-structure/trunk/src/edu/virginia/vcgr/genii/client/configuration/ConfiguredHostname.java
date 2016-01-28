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
package edu.virginia.vcgr.genii.client.configuration;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.configuration.XMLConfiguration;

import edu.virginia.vcgr.appmgr.net.Hostname;
import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.InstallationProperties;

/**
 * a class derived from our Hostname class that overrides methods to provide information from the client or container configuration. this
 * mainly affects what name is reported as "the" hostname for this machine.
 */
public class ConfiguredHostname extends Hostname
{
	public static Log _logger = LogFactory.getLog(ConfiguredHostname.class);

	public ConfiguredHostname()
	{
		super();
	}

	public ConfiguredHostname(String givenAddress)
	{
		super(givenAddress);
	}

	/**
	 * anything that wants to report the proper hostname, based on the client/container configuration, should use this method.
	 */
	static public ConfiguredHostname lookupHost(String givenHostname)
	{
		return new ConfiguredHostname(givenHostname);
	}

	@Override
	public String getHostnameOverride()
	{
		String toReturn = InstallationProperties.getInstallationProperties()
			.getProperty(edu.virginia.vcgr.appmgr.net.Hostname._EXTERNAL_HOSTNAME_OVERRIDE_PROPERTY);
		if (toReturn == null)
			toReturn = getGlobalProperty(edu.virginia.vcgr.appmgr.net.Hostname._EXTERNAL_HOSTNAME_OVERRIDE_PROPERTY);

		// consider a blank name to be a non-established one.
		if ((toReturn != null) && (toReturn.length() == 0)) {
			toReturn = null;
		}

		if (_logger.isTraceEnabled())
			_logger.debug("about to return override host name of: " + toReturn);

		return toReturn;
	}

	@Override
	public String getEPRAddressingMode()
	{
		return getGlobalProperty(edu.virginia.vcgr.appmgr.net.Hostname._EPR_ADDRESSING_MODE_PROPERTY);
	}

	/**
	 * only used by our configuration-specific hostname class, to find the properties defined in the gffs configuration files.
	 */
	static private String getGlobalProperty(String propertyName)
	{
		try {
			XMLConfiguration conf = ConfigurationManager.getCurrentConfiguration().getRoleSpecificConfiguration();
			if (conf == null)
				return null;
			Properties props = (Properties) conf.retrieveSection(GenesisIIConstants.GLOBAL_PROPERTY_SECTION_NAME);
			return props.getProperty(propertyName);
		} catch (ConfigurationException ce) {
			if (_logger.isDebugEnabled()) {
				_logger.debug(ce);
			}
			return null;
		}
	}

}
