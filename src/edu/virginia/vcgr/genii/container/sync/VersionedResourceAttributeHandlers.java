package edu.virginia.vcgr.genii.container.sync;

import org.apache.axis.message.MessageElement;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.ser.ObjectDeserializer;
import edu.virginia.vcgr.genii.container.attrs.AbstractAttributeHandler;
import edu.virginia.vcgr.genii.container.attrs.AttributePackage;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;

public class VersionedResourceAttributeHandlers extends AbstractAttributeHandler
{
	public VersionedResourceAttributeHandlers(AttributePackage pkg) throws NoSuchMethodException
	{
		super(pkg);
	}

	public MessageElement getVersionVectorAttr() throws ResourceUnknownFaultType, ResourceException
	{
		ResourceKey rKey = ResourceManager.getCurrentResource();
		IResource resource = (IResource) rKey.dereference();
		VersionVector vector = (VersionVector) resource.getProperty(SyncProperty.VERSION_VECTOR_PROP_NAME);
		String text = (vector == null ? "" : vector.toString());
		return new MessageElement(SyncProperty.VERSION_VECTOR_QNAME, text);
	}

	public MessageElement getReplicationStatusAttr() throws ResourceUnknownFaultType, ResourceException
	{
		return new MessageElement(SyncProperty.REPLICATION_STATUS_QNAME, "true");
	}

	public MessageElement getUnlinkedReplicaAttr() throws ResourceUnknownFaultType, ResourceException
	{
		ResourceKey rKey = ResourceManager.getCurrentResource();
		IResource resource = (IResource) rKey.dereference();
		String value = (String) resource.getProperty(SyncProperty.UNLINKED_REPLICA_PROP_NAME);
		if (value == null)
			return null;
		return new MessageElement(SyncProperty.UNLINKED_REPLICA_QNAME, value);
	}

	public void setUnlinkedReplicaAttr(MessageElement element) throws ResourceException, ResourceUnknownFaultType
	{
		String value = ObjectDeserializer.toObject(element, String.class);
		ResourceKey rKey = ResourceManager.getCurrentResource();
		IResource resource = (IResource) rKey.dereference();
		if (!resource.isServiceResource()) {
			resource.setProperty(SyncProperty.UNLINKED_REPLICA_PROP_NAME, value);
		}
	}

	@Override
	protected void registerHandlers() throws NoSuchMethodException
	{
		addHandler(SyncProperty.VERSION_VECTOR_QNAME, "getVersionVectorAttr");
		addHandler(SyncProperty.REPLICATION_STATUS_QNAME, "getReplicationStatusAttr");
		addHandler(SyncProperty.UNLINKED_REPLICA_QNAME, "getUnlinkedReplicaAttr", "setUnlinkedReplicaAttr");
	}
}
