package edu.virginia.vcgr.genii.client;

import javax.xml.namespace.QName;

public class GenesisIIConstants
{
	static public final String GENESISII_NS =
		"http://vcgr.cs.virginia.edu/Genesis-II";
	
	/// System property to indicate the installtion location 
	static public final String INSTALL_DIR_SYSTEM_PROPERTY = 
		"edu.virginia.vcgr.genii.install-base-dir";
	
	static public final String DEPLOYMENT_NAME_PROPERTY =
		"edu.virginia.vcgr.genii.client.deployment-name";
	
	static public final long CredentialExpirationMillis = 1000 * 60 * 60 * 24; // valid 24 hours
 
	static public final String NAMING_CLIENT_CONFORMANCE_PROPERTY = "IsWSNamingClient";
	
	static public final String REGISTERED_TOPICS_ATTR =
		"registered-topic";
	static public QName REGISTERED_TOPICS_ATTR_QNAME =
		new QName(GENESISII_NS, REGISTERED_TOPICS_ATTR);
	
	static public final String IMPLEMENTED_PORT_TYPES_ATTR =
		"implemented-port-types";
	static public QName IMPLEMENTED_PORT_TYPES_ATTR_QNAME =
		new QName(GENESISII_NS, IMPLEMENTED_PORT_TYPES_ATTR);

	static public QName AUTHZ_CONFIG_ATTR_QNAME =
		new QName("http://vcgr.cs.virginia.edu/genii/2006/12/security", "AuthZConfig");
	static public final String AUTHZ_CONFIG_ATTR =
		AUTHZ_CONFIG_ATTR_QNAME.getLocalPart();
	
	static public final String SCHED_TERM_TIME_PROPERTY_NAME =
		"scheduled-termintation-time";
	
	static public QName SCHED_TERM_TIME_QNAME =
		new QName(GENESISII_NS, SCHED_TERM_TIME_PROPERTY_NAME);
	
	static public QName RESOURCE_ENDPOINT_ATTR_QNAME =
		new QName(GENESISII_NS, "resource-endpoint");
	
	static public QName GLOBAL_PROPERTY_SECTION_NAME =
		new QName(GenesisIIConstants.GENESISII_NS, "global-properties");
	
	static public QName CONTEXT_INFORMATION_QNAME =
		new QName(GenesisIIConstants.GENESISII_NS, "calling-context");
	
	static public final String OGSA_BSP_NS =
		"http://schemas.ggf.org/ogsa/2006/01/bsp-core";
	
	static public final String JSDL_NS =
		"http://schemas.ggf.org/jsdl/2005/11/jsdl";
	static public final String JSDL_POSIX_NS =
		"http://schemas.ggf.org/jsdl/2005/11/jsdl-posix";
	static public final String JSDL_HPC_NS =
		"http://schemas.ggf.org/jsdl/2006/07/jsdl-hpcp";
	
	static public final String BES_FACTORY_NS =
		"http://schemas.ggf.org/bes/2006/08/bes-factory";
	
	static public final String EXECUTION_ENGINE_THREAD_POOL_SIZE_PROPERTY =
		"edu.virginia.vcgr.genii.container.production.bes.thread-pool-size";
	
	static public QName SSL_PROPERTIES_SECTION_NAME =
		new QName(GENESISII_NS, "ssl-properties");
	static public final String TRUST_STORE_LOCATION_PROPERTY =
		"edu.virginia.vcgr.genii.client.security.ssl.trust-store-location";
	static public final String TRUST_STORE_TYPE_PROPERTY =
		"edu.virginia.vcgr.genii.client.security.ssl.trust-store-type";
	static public final String TRUST_STORE_TYPE_DEFAULT = "PKCS12";
	static public final String TRUST_STORE_PASSWORD_PROPERTY =
		"edu.virginia.vcgr.genii.client.security.ssl.trust-store-password";
	
	static public QName RESOURCE_IDENTITY_PROPERTIES_SECTION_NAME =
		new QName(GENESISII_NS, "resource-identity");
	static public QName MESSAGE_SECURITY_PROPERTIES_SECTION_NAME =
		new QName(GENESISII_NS, "message-security");
	static public QName AUTHZ_PROPERTIES_SECTION_NAME =
		new QName(GENESISII_NS, "authorization");
	static public final String AUTHZ_ENABLED_CONFIG_PROPERTY = 
		"genii.security.authz.authz-enabled";
	static public final String BOOTSTRAP_OWNER_CERTPATH = 
		"genii.security.authz.bootstrapOwnerCertPath";
	
	static public final String CONTAINER_CERT_ALIAS = "VCGR Container";
	
	static public final String USER_CONFIG_ENVIRONMENT_VARIABLE =
		"GENII_USER_CONFIG_DIR";
	static public final String DEPLOYMENT_NAME_ENVIRONMENT_VARIABLE =
		"GENII_DEPLOYMENT_NAME";

	static public QName RNS_CACHED_METADATA_DOCUMENT_QNAME =
		new QName(GENESISII_NS, "rns-cached-metadata");
}