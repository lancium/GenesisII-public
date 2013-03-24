package edu.virginia.vcgr.genii.client.common;

import java.util.Collection;

import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.common.security.*;
import edu.virginia.vcgr.fsii.security.Permissions;
import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.rp.ResourceProperty;
import edu.virginia.vcgr.genii.client.utils.units.Duration;
import edu.virginia.vcgr.genii.common.MatchingParameter;

public interface GenesisIIBaseRP
{
	static public final String GENII_COMMON_NS = "http://vcgr.cs.virginia.edu/genii/common";
	static public final String MATCHING_PARAMETER_ATTR_NAME = "matching-parameter";
	static public final QName MATCHING_PARAMETER_ATTR_QNAME = new QName(GENII_COMMON_NS, MATCHING_PARAMETER_ATTR_NAME);

	static public final String AUTHZ_CONFIG_NAMESPACE = "http://vcgr.cs.virginia.edu/genii/2008/12/security";
	static public final String AUTHZ_CONFIG_NAME = "AuthZConfig";
	static public final QName AUTHZ_CONFIG_QNAME = new QName(AUTHZ_CONFIG_NAMESPACE, AUTHZ_CONFIG_NAME);

	static public final String PERMISSIONS_STRING_NAME = "Permissions";
	static public final QName PERMISSIONS_STRING_QNAME = new QName(GenesisIIConstants.GENESISII_NS, PERMISSIONS_STRING_NAME);

	@ResourceProperty(namespace = GENII_COMMON_NS, localname = MATCHING_PARAMETER_ATTR_NAME, max = "unbounded")
	public Collection<MatchingParameter> getMatchingParameter();

	@ResourceProperty(namespace = AUTHZ_CONFIG_NAMESPACE, localname = AUTHZ_CONFIG_NAME)
	public AuthZConfig getAuthZConfig();

	@ResourceProperty(namespace = AUTHZ_CONFIG_NAMESPACE, localname = AUTHZ_CONFIG_NAME)
	public void setAuthZConfig(AuthZConfig config);

	@ResourceProperty(namespace = GenesisIIConstants.GENESISII_NS, localname = PERMISSIONS_STRING_NAME, translator = PermissionsStringTranslator.class)
	public Permissions getPermissions();

	@ResourceProperty(namespace = GenesisIIConstants.GENESISII_NS, localname = GenesisIIConstants.CACHE_COHERENCE_WINDOW_ATTR_NAME, translator = DurationTranslator.class)
	public Duration getCacheCoherenceWindow();

	@ResourceProperty(namespace = GenesisIIConstants.GENESISII_NS, localname = GenesisIIConstants.CACHE_COHERENCE_WINDOW_ATTR_NAME, translator = DurationTranslator.class)
	public void setCacheCoherenceWindow(Duration aDur);
}