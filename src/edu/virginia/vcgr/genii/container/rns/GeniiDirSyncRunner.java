package edu.virginia.vcgr.genii.container.rns;

import java.util.Collection;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.RNSTopics;

import edu.virginia.vcgr.genii.common.GeniiCommon;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.rns.IRNSResource;
import edu.virginia.vcgr.genii.container.rns.InternalEntry;
import edu.virginia.vcgr.genii.container.sync.ReplicationItem;
import edu.virginia.vcgr.genii.container.sync.ReplicationThread;
import edu.virginia.vcgr.genii.container.sync.ResourceSyncRunner;
import edu.virginia.vcgr.genii.container.sync.VersionedResourceUtils;

public class GeniiDirSyncRunner implements ResourceSyncRunner
{
	static private Log _logger = LogFactory.getLog(GeniiDirSyncRunner.class);

	public void doSync(IResource vResource, EndpointReferenceType primaryEPR, EndpointReferenceType myEPR,
		ReplicationThread replicator) throws Throwable
	{
		// Synchronize the attributes -- resolver and replication policies.
		IRNSResource resource = (IRNSResource) vResource;
		GeniiCommon proxy = ClientUtils.createProxy(GeniiCommon.class, primaryEPR);
		MessageElement element;

		element = VersionedResourceUtils.getResourceProperty(proxy, GeniiDirPolicy.RESOLVER_POLICY_QNAME);
		byte[] data = null;
		if (element != null) {
			EndpointReferenceType resolverPolicy = (EndpointReferenceType) element.getObjectValue(EndpointReferenceType.class);
			if (resolverPolicy != null)
				data = EPRUtils.toBytes(resolverPolicy);
		}
		resource.setProperty(GeniiDirPolicy.RESOLVER_POLICY_PROP_NAME, data);

		element = VersionedResourceUtils.getResourceProperty(proxy, GeniiDirPolicy.REPLICATION_POLICY_QNAME);
		String value = null;
		if (element != null)
			value = element.getValue();
		resource.setProperty(GeniiDirPolicy.REPLICATION_POLICY_PROP_NAME, value);

		// Get a name -> EPR map of the contents of the target resource.
		SortedMap<String, EndpointReferenceType> targetMap = new TreeMap<String, EndpointReferenceType>();
		RNSPath sourceDir = new RNSPath(primaryEPR);
		Collection<RNSPath> contents = sourceDir.listContents();
		if (_logger.isDebugEnabled())
			_logger.debug("GeniiDirSyncRunner: contents.size=" + contents.size());
		for (RNSPath entry : contents) {
			EndpointReferenceType entryEPR = entry.getEndpoint();
			ReplicationItem item = AutoReplicate.autoReplicate(resource, entryEPR);
			if (item != null) {
				entryEPR = item.localEPR;
				if (item.runner != null)
					replicator.add(item);
			}
			targetMap.put(entry.getName(), entryEPR);
		}
		// Get a name -> EPR map of the current contents of this resource.
		SortedMap<String, EndpointReferenceType> currentMap = new TreeMap<String, EndpointReferenceType>();
		Collection<InternalEntry> entries = resource.retrieveEntries(null);
		for (InternalEntry entry : entries) {
			currentMap.put(entry.getName(), entry.getEntryReference());
		}
		// Synchronize the data
		for (Map.Entry<String, EndpointReferenceType> entry : targetMap.entrySet()) {
			String name = entry.getKey();
			EndpointReferenceType eprValue = entry.getValue();
			EndpointReferenceType currentValue = currentMap.remove(name);
			if (currentValue != null) {
				if (eprValue.equals(currentValue))
					continue;
				resource.removeEntries(name);
			}
			resource.addEntry(new InternalEntry(name, eprValue));
		}
		for (String extraName : currentMap.keySet()) {
			resource.removeEntries(extraName);
		}
	}

	public TopicPath getSyncTopic()
	{
		return RNSTopics.RNS_OPERATION_TOPIC;
	}
}
