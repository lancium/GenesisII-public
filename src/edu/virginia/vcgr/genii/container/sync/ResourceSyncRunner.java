package edu.virginia.vcgr.genii.container.sync;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;
import edu.virginia.vcgr.genii.container.resource.IResource;

public interface ResourceSyncRunner
{
	/**
	 * Get the state data from the primary instance. Save it in the local database and filesystem.
	 */
	public void doSync(IResource resource, EndpointReferenceType primaryEPR, EndpointReferenceType myEPR,
		ReplicationThread replicator) throws Throwable;

	/**
	 * Return the topic to which this replica must subscribe.
	 */
	public TopicPath getSyncTopic();
}
