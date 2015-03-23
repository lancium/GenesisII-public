package edu.virginia.vcgr.genii.client;

import edu.virginia.vcgr.genii.client.configuration.WebContainerConstants;

public class InstallationConstants
{
	static public final String INSTALLATION_PROPERTIES_FILENAME = "installation.properties";

	static public final String CONTAINER_PORT_PROPERTY = WebContainerConstants.LISTEN_PORT_PROP;

	static public final String LOCAL_CERTS_DIRECTORY_PROPERTY = "edu.virginia.vcgr.genii.container.security.certs-dir";
	static public final String OWNER_CERTS_DIRECTORY_NAME = "default-owners";

	static public final String OWNER_CERT_FILE_NAME = "owner.cer";

	/*
	 * the file containing information about the current grid deployment. will only be definitively
	 * valid for an installed system; the build just copies an example deployment file into place.
	 */
	final public static String DEPLOYMENT_PROPERTIES_FILE = "current.deployment";

	/*
	 * the property name for the grid's "simple name" which is stored in the deployment properties
	 * file. knowing the value of this should let us guess the proper login proxy.
	 */
	final public static String GRID_NAME_SETTING = "genii.simple-name";
}
