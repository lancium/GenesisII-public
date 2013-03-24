package edu.virginia.vcgr.genii.container.resolver;

import java.io.EOFException;
import java.io.InputStream;
import java.io.ObjectInputStream;

import org.apache.axis.types.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.byteio.ByteIOStreamFactory;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;

import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.sync.ReplicationThread;
import edu.virginia.vcgr.genii.container.sync.ResourceSyncRunner;

public class GeniiResolverSyncRunner implements ResourceSyncRunner
{
	static private Log _logger = LogFactory.getLog(GeniiResolverSyncRunner.class);

	public void doSync(IResource vResource, EndpointReferenceType primaryEPR, EndpointReferenceType myEPR,
		ReplicationThread replicator) throws Throwable
	{
		// Synchronize the attributes -- resolver and replication policies.
		IGeniiResolverResource resource = (IGeniiResolverResource) vResource;
		ObjectInputStream objstream = null;
		try {
			InputStream istream = ByteIOStreamFactory.createInputStream(primaryEPR);
			objstream = new ObjectInputStream(istream);
			while (true) {
				Object object = null;
				try {
					object = objstream.readObject();
				} catch (EOFException eof) {
					break;
				}
				URI targetEPI = new URI((String) object);
				int targetID = objstream.readInt();
				byte[] data = (byte[]) objstream.readObject();
				EndpointReferenceType targetEPR = EPRUtils.fromBytes(data);
				if (_logger.isDebugEnabled())
					_logger.debug("resolver: " + targetEPR.getAddress());
				resource.addTargetEPR(targetEPI, targetID, targetEPR);
				GeniiResolverUtils.createTerminateSubscription(targetID, targetEPR, myEPR, resource);
			}
		} finally {
			StreamUtils.close(objstream);
		}
		GeniiResolverUtils.initializeNextTargetIDinReplica(resource);
	}

	public TopicPath getSyncTopic()
	{
		return ResolverTopics.RESOLVER_UPDATE_TOPIC;
	}
}
