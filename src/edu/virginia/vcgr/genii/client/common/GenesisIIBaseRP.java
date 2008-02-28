package edu.virginia.vcgr.genii.client.common;

import edu.virginia.vcgr.genii.client.rp.ResourceProperty;
import edu.virginia.vcgr.genii.common.security.AuthZConfig;

public interface GenesisIIBaseRP
{
	@ResourceProperty(
		namespace = "http://vcgr.cs.virginia.edu/genii/2006/12/security",
		localname = "AuthZConfig")
	public AuthZConfig getAuthZConfig();
	
	@ResourceProperty(
		namespace = "http://vcgr.cs.virginia.edu/genii/2006/12/security",
		localname = "AuthZConfig")
	public void setAuthZConfig(AuthZConfig config);
}