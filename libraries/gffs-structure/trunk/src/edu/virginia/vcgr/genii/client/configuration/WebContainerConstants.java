package edu.virginia.vcgr.genii.client.configuration;

public interface WebContainerConstants
{
	static public final String LISTEN_PORT_PROP = "edu.virginia.vcgr.genii.container.listen-port";
	static public final String DPAGES_PORT_PROP = "edu.virginia.vcgr.genii.container.dpages-port";

	static public final String MAX_ACCEPT_THREADS_PROP = "edu.virginia.vcgr.genii.container.max-acceptor-threads";

	static public final String USE_SSL_PROP = "edu.virginia.vcgr.genii.container.listen-port.use-ssl";

	static public final String TRUST_SELF_SIGNED = "edu.virginia.vcgr.genii.container.trust-self-signed";
}