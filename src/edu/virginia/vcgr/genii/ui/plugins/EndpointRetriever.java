package edu.virginia.vcgr.genii.ui.plugins;

import java.util.Collection;

import edu.virginia.vcgr.genii.client.rns.RNSPath;

public interface EndpointRetriever
{
	public Collection<RNSPath> getTargetEndpoints();
	
	public void refresh();
	public void refreshParent();
}