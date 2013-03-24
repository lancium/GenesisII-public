package edu.virginia.vcgr.genii.client.dair;

import java.util.HashMap;
import java.util.Hashtable;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;

public class DAIRUtils
{

	static final protected String _DB_DRIVER_NAME = "_DB_DRIVER_NAME";
	static final protected String _CONNECT_STRING = "_CONNECT_STRING";
	static final protected String _USERNAME = "_USERNAME";
	static final protected String _PASSWORD = "_PASSWORD";

	@SuppressWarnings("unused")
	static private Log _logger = LogFactory.getLog(DAIRUtils.class);

	static public class SQLAccessCombinedInitInfo
	{
		private String _driver = null;
		private String _connect_string = null;
		private String _username = null;
		private String _password = null;

		public SQLAccessCombinedInitInfo(String driver, String connect_string, String username, String password)
		{
			_driver = driver;
			_connect_string = connect_string;
			_username = username;
			_password = password;

		}

		public String getDriver()
		{
			return _driver;
		}

		public String getConnectString()
		{
			return _connect_string;
		}

		public String getUsername()
		{
			return _username;
		}

		public String getPassword()
		{
			return _password;
		}
	}

	static public MessageElement[] createCreationProperties(String driver, String connect_string, String username,
		String password)
	{
		MessageElement[] any = new MessageElement[4];
		any[0] = new MessageElement(new QName(GenesisIIConstants.GENESISII_NS, _DB_DRIVER_NAME), driver);
		any[1] = new MessageElement(new QName(GenesisIIConstants.GENESISII_NS, _CONNECT_STRING), connect_string);
		any[2] = new MessageElement(new QName(GenesisIIConstants.GENESISII_NS, _USERNAME), username);
		any[3] = new MessageElement(new QName(GenesisIIConstants.GENESISII_NS, _PASSWORD), password);
		return any;
	}

	@SuppressWarnings("unchecked")
	static public SQLAccessCombinedInitInfo extractCreationProperties(HashMap<QName, Object> properties)
	{

		String driver = null;
		String connect_string = null;
		String username = null;
		String password = null;

		if (properties == null)
			throw new IllegalArgumentException("Can't have a null SQLAccessCombined creation properties parameter.");

		// get driver
		/*
		 * it's not getting the correct value for the driver element there's something messed up
		 * with the creationProperties
		 */

		Hashtable<String, String> table = (Hashtable<String, String>) properties.get(new QName(GenesisIIConstants.GENESISII_NS,
			"creation-properties"));
		driver = table.get(_DB_DRIVER_NAME);
		connect_string = table.get(_CONNECT_STRING);
		username = table.get(_USERNAME);
		password = table.get(_PASSWORD);

		return new SQLAccessCombinedInitInfo(driver, connect_string, username, password);
	}

}
