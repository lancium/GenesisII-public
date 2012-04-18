package edu.virginia.vcgr.genii.container.sync;

import org.apache.axis.message.MessageElement;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.attrs.AbstractAttributeHandler;
import edu.virginia.vcgr.genii.container.attrs.AttributePackage;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;

public class VersionedResourceAttributeHandlers extends AbstractAttributeHandler
{
	public VersionedResourceAttributeHandlers(AttributePackage pkg)
		throws NoSuchMethodException
	{
		super(pkg);
	}

	public MessageElement getVersionVectorAttr()
		throws ResourceUnknownFaultType, ResourceException
	{
		ResourceKey rKey = ResourceManager.getCurrentResource();
		IResource resource = (IResource)rKey.dereference();
		VersionVector vector = (VersionVector) resource.getProperty(SyncProperty.VERSION_VECTOR_PROP_NAME);
		String text = (vector == null ? "" : vector.toString());
		return new MessageElement(SyncProperty.VERSION_VECTOR_QNAME, text);
	}

	public MessageElement getReplicationStatusAttr()
		throws ResourceUnknownFaultType, ResourceException
	{
		return new MessageElement(SyncProperty.REPLICATION_STATUS_QNAME, "true");
	}
	
	@Override
	protected void registerHandlers() throws NoSuchMethodException
	{
		addHandler(SyncProperty.VERSION_VECTOR_QNAME, "getVersionVectorAttr");
		addHandler(SyncProperty.REPLICATION_STATUS_QNAME, "getReplicationStatusAttr");

	}
}
