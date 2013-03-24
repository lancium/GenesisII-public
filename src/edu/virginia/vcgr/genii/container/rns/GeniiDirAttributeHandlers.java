package edu.virginia.vcgr.genii.container.rns;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.ResourceException;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.container.attrs.AbstractAttributeHandler;
import edu.virginia.vcgr.genii.container.attrs.AttributePackage;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;

public class GeniiDirAttributeHandlers extends AbstractAttributeHandler
{
	public GeniiDirAttributeHandlers(AttributePackage pkg) throws NoSuchMethodException
	{
		super(pkg);
	}

	@Override
	protected void registerHandlers() throws NoSuchMethodException
	{
		addHandler(GeniiDirPolicy.RESOLVER_POLICY_QNAME, "getResolverPolicyAttr", "setResolverPolicyAttr");
		addHandler(GeniiDirPolicy.REPLICATION_POLICY_QNAME, "getReplicationPolicyAttr", "setReplicationPolicyAttr");
	}

	public MessageElement getResolverPolicyAttr() throws ResourceUnknownFaultType, ResourceException
	{
		IResource resource = ResourceManager.getCurrentResource().dereference();
		byte[] data = (byte[]) resource.getProperty(GeniiDirPolicy.RESOLVER_POLICY_PROP_NAME);
		if (data == null)
			return null;
		EndpointReferenceType value = EPRUtils.fromBytes(data);
		return new MessageElement(GeniiDirPolicy.RESOLVER_POLICY_QNAME, value);
	}

	public void setResolverPolicyAttr(MessageElement element) throws ResourceException, ResourceUnknownFaultType
	{
		IResource resource = ResourceManager.getCurrentResource().dereference();
		EndpointReferenceType value;
		try {
			value = (EndpointReferenceType) element.getObjectValue(EndpointReferenceType.class);
		} catch (Exception exception) {
			return;
		}
		byte[] data = null;
		if (value != null)
			data = EPRUtils.toBytes(value);
		resource.setProperty(GeniiDirPolicy.RESOLVER_POLICY_PROP_NAME, data);
	}

	public MessageElement getReplicationPolicyAttr() throws ResourceUnknownFaultType, ResourceException
	{
		IResource resource = ResourceManager.getCurrentResource().dereference();
		String value = (String) resource.getProperty(GeniiDirPolicy.REPLICATION_POLICY_PROP_NAME);
		if (value == null)
			return null;
		return new MessageElement(GeniiDirPolicy.REPLICATION_POLICY_QNAME, value);
	}

	public void setReplicationPolicyAttr(MessageElement element) throws ResourceException, ResourceUnknownFaultType
	{
		IResource resource = ResourceManager.getCurrentResource().dereference();
		String value;
		try {
			value = (String) element.getObjectValue(String.class);
		} catch (Exception exception) {
			return;
		}
		resource.setProperty(GeniiDirPolicy.REPLICATION_POLICY_PROP_NAME, value);
	}
}
