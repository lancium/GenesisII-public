package edu.virginia.vcgr.genii.container.rns;

import org.apache.axis.message.MessageElement;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.rns.RNSConstants;
import edu.virginia.vcgr.genii.container.attrs.AbstractAttributeHandler;
import edu.virginia.vcgr.genii.container.attrs.AttributePackage;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;

public class RNSAttributesHandler extends AbstractAttributeHandler {

	public RNSAttributesHandler(AttributePackage pkg) throws NoSuchMethodException {
		super(pkg);
	}

	@Override
	protected void registerHandlers() throws NoSuchMethodException {
		addHandler(RNSConstants.ELEMENT_COUNT_QNAME, "getElementCount");
	}
	
	public MessageElement getElementCount() throws ResourceUnknownFaultType, 
			ResourceException {
		IRNSResource resource = null;
		resource = (IRNSResource)(ResourceManager.getCurrentResource().dereference());
		Object elementCount = null;
		if (!resource.isServiceResource()) {
			elementCount = resource.getProperty(IRNSResource.ELEMENT_COUNT_PROPERTY);
		}
		return new MessageElement(RNSConstants.ELEMENT_COUNT_QNAME, elementCount);
	}
}
