package edu.virginia.vcgr.genii.client.configuration;

public interface KeystoreSecurityConstants
{
	static public final String TRUST_STORE_TYPE_DEFAULT = "PKCS12";

	public interface Client
	{
		static public final String CLIENT_RSA_KEY_LENGTH_PROP =
			"edu.virginia.vcgr.genii.client.security.message.rsa-key-length";
		static public final String MESSAGE_MIN_CONFIG_PROP = "edu.virginia.vcgr.genii.client.security.message.min-config";
		static public final String RESOURCE_IDENTITY_TRUST_STORE_LOCATION_PROP =
			"edu.virginia.vcgr.genii.client.security.resource-identity.trust-store-location";
		static public final String RESOURCE_IDENTITY_TRUST_STORE_TYPE_PROP =
			"edu.virginia.vcgr.genii.client.security.resource-identity.trust-store-type";
		static public final String RESOURCE_IDENTITY_TRUST_STORE_PASSWORD_PROP =
			"edu.virginia.vcgr.genii.client.security.resource-identity.trust-store-password";
		static public final String SSL_TRUST_STORE_LOCATION_PROP =
			"edu.virginia.vcgr.genii.client.security.ssl.trust-store-location";
		static public final String SSL_TRUST_STORE_TYPE_PROP = "edu.virginia.vcgr.genii.client.security.ssl.trust-store-type";
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
		static public final String SSL_KEY_STORE_PROP = "edu.virginia.vcgr.genii.container.security.ssl.key-store";
		static public final String SSL_KEY_STORE_TYPE_PROP = "edu.virginia.vcgr.genii.container.security.ssl.key-store-type";
		static public final String SSL_KEY_PASSWORD_PROP = "edu.virginia.vcgr.genii.container.security.ssl.key-password";
		static public final String SSL_KEY_STORE_PASSWORD_PROP =
			"edu.virginia.vcgr.genii.container.security.ssl.key-store-password";
	}

	public class Kerberos
	{
		// the property prefix for finding a keytab for a realm (this must be done per STS host).
		static public final String KERBEROS_KEYTAB_STRING = "gffs-sts.kerberos.keytab.";

		/**
		 * generates the appropriate configuration item name for looking up the keytab for a
		 * particular Kerberos realm.
		 */
		static public String keytabPropertyForRealm(String realm)
		{
			return KERBEROS_KEYTAB_STRING + realm;
		}

		// property prefix for finding the principal to authorize in the realm (done per STS host).
		static public final String KERBEROS_PRINCIPAL_STRING = "gffs-sts.kerberos.principal.";

		/**
		 * generates the appropriate configuration item name for looking up the principal for
		 * authorization within a particular Kerberos realm.
		 */
		static public String principalPropertyForRealm(String realm)
		{
			return KERBEROS_PRINCIPAL_STRING + realm;
		}
	}
}
