package edu.virginia.vcgr.genii.client.configuration;

public interface SecurityConstants
{
	static public final String TRUST_STORE_TYPE_DEFAULT = "PKCS12";
	
	public interface Client
	{
		static public final String CLIENT_RSA_KEY_LENGTH_PROP =
			"edu.virginia.vcgr.genii.client.security.message.rsa-key-length";
		static public final String MESSAGE_MIN_CONFIG_PROP =
			"edu.virginia.vcgr.genii.client.security.message.min-config";
		static public final String RESOURCE_IDENTITY_TRUST_STORE_LOCATION_PROP =
			"edu.virginia.vcgr.genii.client.security.resource-identity.trust-store-location";
		static public final String RESOURCE_IDENTITY_TRUST_STORE_TYPE_PROP =
			"edu.virginia.vcgr.genii.client.security.resource-identity.trust-store-type";
		static public final String RESOURCE_IDENTITY_TRUST_STORE_PASSWORD_PROP =
			"edu.virginia.vcgr.genii.client.security.resource-identity.trust-store-password";
		static public final String SSL_TRUST_STORE_LOCATION_PROP =
			"edu.virginia.vcgr.genii.client.security.ssl.trust-store-location";
		static public final String SSL_TRUST_STORE_TYPE_PROP =
			"edu.virginia.vcgr.genii.client.security.ssl.trust-store-type";
		static public final String SSL_TRUST_STORE_PASSWORD_PROP =
			"edu.virginia.vcgr.genii.client.security.ssl.trust-store-password";
		static public final String SSL_TRUSTED_CERTIFICATES_LOCATION_PROP = 
			"edu.virginia.vcgr.genii.client.security.ssl.trusted-certificates.location";
	}

	public interface Container
	{
		static public final String RESOURCE_IDENTITY_USE_OGSA_EAP_PROP =
			"edu.virginia.vcgr.genii.container.security.resource-identity.use-ogsa-eap";
		static public final String RESOURCE_IDENTITY_KEY_STORE_PROP =
			"edu.virginia.vcgr.genii.container.security.resource-identity.key-store";
		static public final String RESOURCE_IDENTITY_KEY_STORE_TYPE_PROP =
			"edu.virginia.vcgr.genii.container.security.resource-identity.key-store-type";
		static public final String RESOURCE_IDENTITY_KEY_PASSWORD_PROP =
			"edu.virginia.vcgr.genii.container.security.resource-identity.key-password";
		static public final String RESOURCE_IDENTITY_KEY_STORE_PASSWORD_PROP =
			"edu.virginia.vcgr.genii.container.security.resource-identity.key-store-password";
		static public final String RESOURCE_IDENTITY_CONTAINER_ALIAS_PROP =
			"edu.virginia.vcgr.genii.container.security.resource-identity.container-alias";
		static public final String RESOURCE_IDENTITY_DEFAULT_CERT_LIFETIME_PROP =
			"edu.virginia.vcgr.genii.container.security.resource-identity.default-certificate-lifetime";
		static public final String SSL_KEY_STORE_PROP =
			"edu.virginia.vcgr.genii.container.security.ssl.key-store";
		static public final String SSL_KEY_STORE_TYPE_PROP =
			"edu.virginia.vcgr.genii.container.security.ssl.key-store-type";
		static public final String SSL_KEY_PASSWORD_PROP =
			"edu.virginia.vcgr.genii.container.security.ssl.key-password";
		static public final String SSL_KEY_STORE_PASSWORD_PROP =
			"edu.virginia.vcgr.genii.container.security.ssl.key-store-password";
	}
}
