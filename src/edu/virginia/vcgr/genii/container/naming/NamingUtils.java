package edu.virginia.vcgr.genii.container.naming;

import java.io.IOException;

import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.context.ContextManager;

public class NamingUtils
{
	static public boolean isWSNamingAwareClient()
	{
		try {
			Boolean b = Boolean.parseBoolean((String) ContextManager.getExistingContext().getSingleValueProperty(
				GenesisIIConstants.NAMING_CLIENT_CONFORMANCE_PROPERTY));
			return (b != null && b.booleanValue());
		} catch (IOException fnfe) {
			return false;
		} catch (ConfigurationException ce) {
			return false;
		}
	}
}