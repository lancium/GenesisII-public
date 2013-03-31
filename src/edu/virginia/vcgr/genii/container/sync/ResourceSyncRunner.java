package edu.virginia.vcgr.genii.container.sync;

import java.util.Collection;

import org.apache.axis.message.MessageElement;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.resource.IResource;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;

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

	/**
	 * Return attributes that are used to the govern behavior of a replica EPR creation process.
	 * */
	public Collection<MessageElement> getDefaultAttributes(EndpointReferenceType primaryEPR);
}
