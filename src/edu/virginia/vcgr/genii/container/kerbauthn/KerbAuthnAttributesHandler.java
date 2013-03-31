package edu.virginia.vcgr.genii.container.kerbauthn;

import org.apache.axis.message.MessageElement;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;

import edu.virginia.vcgr.genii.client.resource.IResource;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.attrs.AbstractAttributeHandler;
import edu.virginia.vcgr.genii.container.attrs.AttributePackage;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.security.SecurityConstants;

/*
 * This attribute handler has been added to make Kerberos IDP specific attributes retrievable within STS 
 * replication feature. Note that the IDP name is the sensitive attribute for a Kerberos IDP resource, which
 * is taken care of in common-STS-attribute-handler class, not the realm and KDB attributes. Hence, although 
 * this handler is added for only replication purpose. We added no additional security checking that might 
 * restrict access to its methods beyond what is already employed by GenesisIIBase class's default access 
 * checking code.  
 * */
public class KerbAuthnAttributesHandler extends AbstractAttributeHandler
{

	public KerbAuthnAttributesHandler(AttributePackage pkg) throws NoSuchMethodException
	{
		super(pkg);
	}

	@Override
	protected void registerHandlers() throws NoSuchMethodException
	{
		addHandler(SecurityConstants.NEW_KERB_IDP_REALM_QNAME, "getRealmAttr");
		addHandler(SecurityConstants.NEW_KERB_IDP_KDC_QNAME, "getKdcAttr");
	}

	public MessageElement getRealmAttr() throws ResourceUnknownFaultType, ResourceException
	{
		ResourceKey resourceKey = ResourceManager.getCurrentResource();
		IResource resource = resourceKey.dereference();
		String realm = (String) resource.getProperty(SecurityConstants.NEW_KERB_IDP_REALM_QNAME.getLocalPart());
		return new MessageElement(SecurityConstants.NEW_KERB_IDP_REALM_QNAME, realm);
	}

	public MessageElement getKdcAttr() throws ResourceUnknownFaultType, ResourceException
	{
		ResourceKey resourceKey = ResourceManager.getCurrentResource();
		IResource resource = resourceKey.dereference();
		String kdc = (String) resource.getProperty(SecurityConstants.NEW_KERB_IDP_KDC_QNAME.getLocalPart());
		return new MessageElement(SecurityConstants.NEW_KERB_IDP_KDC_QNAME, kdc);
	}
}
