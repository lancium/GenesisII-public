package edu.virginia.vcgr.genii.client;

import edu.virginia.vcgr.genii.client.configuration.WebContainerConstants;

public class InstallationConstants
{
	static public final String INSTALLATION_PROPERTIES_FILENAME = "installation.properties";

	static public final String CONTAINER_PORT_PROPERTY = WebContainerConstants.LISTEN_PORT_PROP;

	static public final String LOCAL_CERTS_DIRECTORY_PROPERTY = "edu.virginia.vcgr.genii.container.security.certs-dir";
	static public final String OWNER_CERTS_DIRECTORY_NAME = "default-owners";

	// static public final String GENII_DEPLOYMENT_NAME_PROPERTY =
	// ContainerProperties.GENII_DEPLOYMENT_NAME_PROPERTY_NAME;

	static public final String OWNER_CERT_FILE_NAME = "owner.cer";
}
