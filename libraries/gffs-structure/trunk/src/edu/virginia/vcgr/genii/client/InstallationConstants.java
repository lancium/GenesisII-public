package edu.virginia.vcgr.genii.client;

import edu.virginia.vcgr.genii.client.configuration.Hostname;
import edu.virginia.vcgr.genii.client.configuration.KeystoreSecurityConstants;
import edu.virginia.vcgr.genii.client.configuration.WebContainerConstants;

public class InstallationConstants
{
	static public final String INSTALLATION_PROPERTIES_FILENAME = "installation.properties";
	/*
	 * many constants used with the InstallationProperties come from KeystoreSecurityConstants, from
	 * ContainerProperties and from WebContainerConstants.
	 */
	static public final String TLS_KEY_PASSWORD_PROPERTY = KeystoreSecurityConstants.Container.SSL_KEY_PASSWORD_PROP;
	static public final String TLS_KEYSTORE_PASSWORD_PROPERTY = KeystoreSecurityConstants.Container.SSL_KEY_STORE_PASSWORD_PROP;
	static public final String TLS_KEYSTORE_FILE_PROPERTY = KeystoreSecurityConstants.Container.SSL_KEY_STORE_PROP;
	static public final String TLS_KEYSTORE_TYPE_PROPERTY = KeystoreSecurityConstants.Container.SSL_KEY_STORE_TYPE_PROP;
	static public final String CONTAINER_HOSTNAME_PROPERTY = Hostname._EXTERNAL_HOSTNAME_OVERRIDE_PROPERTY;
	static public final String CONTAINER_PORT_PROPERTY = WebContainerConstants.LISTEN_PORT_PROP;
	static public final String LOCAL_CERTS_DIRECTORY_PROPERTY = "edu.virginia.vcgr.genii.container.security.certs-dir";
	static public final String OWNER_CERTS_DIRECTORY_NAME = "default-owners";
	static public final String SIGNING_KEYSTORE_FILE_PROPERTY =
		KeystoreSecurityConstants.Container.RESOURCE_IDENTITY_KEY_STORE_PROP;
	static public final String GRID_CONNECTION_COMMAND_PROPERTY = ContainerProperties.GRID_CONNECTION_COMMAND_PROPERTY;
	static public final String GENII_DEPLOYMENT_NAME_PROPERTY = ContainerProperties.GENII_DEPLOYMENT_NAME_PROPERTY_NAME;

	static public final String OWNER_CERT_FILE_NAME = "owner.cer";
}
