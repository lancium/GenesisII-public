package edu.virginia.vcgr.genii.client.rns.filters;

import edu.virginia.vcgr.genii.client.rns.RNSPath;

public interface RNSFilter
{
	public boolean matches(RNSPath testEntry);
}