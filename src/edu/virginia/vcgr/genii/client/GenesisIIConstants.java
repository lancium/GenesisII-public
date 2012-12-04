package edu.virginia.vcgr.genii.client;

import javax.xml.namespace.QName;

public class GenesisIIConstants
{
	static public final String GENESISII_STATE_DIR_NAME =
		".genesisII-2.0";
	
	static public final String GENESISII_NS =
		"http://vcgr.cs.virginia.edu/Genesis-II";
	
	static public final String OGSA_BP_NS =
		"http://schemas.ggc.org/ogsa/2006/05/wsrf-bp";
	
	static public final long CredentialExpirationMillis = 
		1000L * 60L * 60L * 24L * 365L * 10L; 	// valid 10 years by default.
 
	static public final long CredentialGoodFromOffset = 
		1000L * 60L * 15L; 				// 15 minutes ago 
	
	static public final long CredentialCacheTimeout =
		1000L * 60L * 60L; 				// 1 hour lifetime in cache

	static public final int MaxDelegationDepth = 10; 

	static public final String NAMING_CLIENT_CONFORMANCE_PROPERTY = "IsWSNamingClient";
	
	static public final String REGISTERED_TOPICS_ATTR =
		"registered-topic";
	static public QName REGISTERED_TOPICS_ATTR_QNAME =
		new QName(GENESISII_NS, REGISTERED_TOPICS_ATTR);
	
	static public QName AUTHZ_CONFIG_ATTR_QNAME =
		new QName("http://vcgr.cs.virginia.edu/genii/2008/12/security", "AuthZConfig");
	static public final String AUTHZ_CONFIG_ATTR =
		AUTHZ_CONFIG_ATTR_QNAME.getLocalPart();
	
	static public final String CACHE_COHERENCE_WINDOW_ATTR_NAME = "CacheCoherenceWindow";
	static public QName CACHE_COHERENCE_WINDOW_ATTR_QNAME =
		new QName(GENESISII_NS, CACHE_COHERENCE_WINDOW_ATTR_NAME);
	
	static public final String SCHED_TERM_TIME_PROPERTY_NAME =
		"scheduled-termintation-time";
	
	static public QName RESOURCE_PROPERTY_NAMES_QNAME =
		new QName(OGSA_BP_NS, "ResourcePropertyNames");
	
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
	
	static public final String AUTHZ_ENABLED_CONFIG_PROPERTY = 
		"genii.security.authz.authz-enabled";
	
	static public final String BOOTSTRAP_OWNER_CERTPATH = 
		"genii.security.authz.bootstrapOwnerCertPath";
	
	static public final String CONTAINER_CERT_ALIAS = "VCGR Container";
	
	static public QName RNS_CACHED_METADATA_DOCUMENT_QNAME =
		new QName(GENESISII_NS, "rns-cached-metadata");
	
	static public final String GENESIS_DAIR_RESULTS = "dair-results";
	
	static final public String COMMAND_FUNCTION_NAME = "function";
	static final public QName COMMAND_FUNCTION_QNAME = new QName(
		WellKnownPortTypes.VCGR_COMMON_PORT_TYPE.getQName().getNamespaceURI(),
		COMMAND_FUNCTION_NAME);
	
	static public QName NOTIFICATION_MESSAGE_ATTRIBUTES_SEPARATOR =
			new QName(GENESISII_NS, "notification-message-attributes-separator");

	static public final QName NOTIFICATION_BROKER_FACTORY_ADDRESS = 
			new QName(GenesisIIConstants.GENESISII_NS, "NotificationBrokerFactory");

	static public final String ENHANCED_NOTIFICATION_BROKER_NS = 
			"http://vcgr.cs.virginia.edu/container/2011/07/enhanced-notification-broker";

	static public final String CLIENT_ID_ATTRIBUTE_NAME = "ClientID";
	static public final QName CLIENT_ID_QNAME = new QName(GENESISII_NS, 
			CLIENT_ID_ATTRIBUTE_NAME);	
}