package edu.virginia.vcgr.genii.client.common;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.rp.ResourceProperty;
import edu.virginia.vcgr.genii.common.security.AuthZConfig;

public interface GenesisIIBaseRP
{
	static public final String AUTHZ_CONFIG_NAMESPACE =
		"http://vcgr.cs.virginia.edu/genii/2006/12/security";
	static public final String AUTHZ_CONFIG_NAME =
		"AuthZConfig";
	static public final QName AUTHZ_CONFIG_QNAME =
		new QName(AUTHZ_CONFIG_NAMESPACE, AUTHZ_CONFIG_NAME);
	
	@ResourceProperty(
		namespace = AUTHZ_CONFIG_NAMESPACE,
		localname = AUTHZ_CONFIG_NAME)
	public AuthZConfig getAuthZConfig();
	
	@ResourceProperty(
		namespace = AUTHZ_CONFIG_NAMESPACE,
		localname = AUTHZ_CONFIG_NAME)
	public void setAuthZConfig(AuthZConfig config);
}