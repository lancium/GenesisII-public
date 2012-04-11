package edu.virginia.vcgr.genii.container.sync;

import javax.xml.namespace.QName;

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

	private QName GetVersionVectorNamespace()
	{
		return new QName(SyncProperty.RESOURCE_SYNC_NS, "VersionVector");
	}

	private String getVersionVector()
		throws ResourceException, ResourceUnknownFaultType
	{
		ResourceKey rKey = ResourceManager.getCurrentResource();
		IResource resource = (IResource)rKey.dereference();
		VersionVector vector = (VersionVector) resource.getProperty(SyncProperty.VERSION_VECTOR_PROP_NAME);
		return(vector == null ? "" : vector.toString());
	}

	public MessageElement getVersionVectorAttr()
		throws ResourceUnknownFaultType, ResourceException
	{
		return new MessageElement(GetVersionVectorNamespace(), getVersionVector());
	}

	@Override
	protected void registerHandlers() throws NoSuchMethodException
	{
		addHandler(GetVersionVectorNamespace(), "getVersionVectorAttr");
	}
}
