package edu.virginia.vcgr.genii.container.rfork;

import java.io.Serializable;

import edu.virginia.vcgr.genii.client.resource.ResourceException;

public interface ResourceForkInformation extends Serializable
{
	public ResourceFork instantiateFork(ResourceForkService forkService)
		throws ResourceException;
}