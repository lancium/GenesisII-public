package edu.virginia.vcgr.genii.container.rfork;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMultiplexer;

public interface ResourceFork
{
	public String getForkPath();
	
	public void registerNotificationHandlers(
		NotificationMultiplexer multiplexer);
	
	public void destroy() throws ResourceException;
	public ResourceForkInformation describe();
}