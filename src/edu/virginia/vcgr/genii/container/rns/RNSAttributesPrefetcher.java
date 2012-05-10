package edu.virginia.vcgr.genii.container.rns;

import java.util.Collection;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.rns.RNSConstants;
import edu.virginia.vcgr.genii.container.common.DefaultGenesisIIAttributesPreFetcher;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;

public class RNSAttributesPrefetcher extends DefaultGenesisIIAttributesPreFetcher<IRNSResource> {

	static private Log _logger = LogFactory.getLog(RNSAttributesPrefetcher.class);

	public RNSAttributesPrefetcher(EndpointReferenceType target)
			throws ResourceException, ResourceUnknownFaultType {
		this((IRNSResource) ResourceManager.getTargetResource(target).dereference());
	}

	public RNSAttributesPrefetcher(IRNSResource resource) {
		super(resource);
	}

	@Override
	protected void fillInAttributes(Collection<MessageElement> attributes) {
		super.fillInAttributes(attributes);
		try {
			Integer elementCount = 
				(Integer) getResource().getProperty(IRNSResource.ELEMENT_COUNT_PROPERTY);
			if (elementCount != null) {
				attributes.add(new MessageElement(RNSConstants.ELEMENT_COUNT_QNAME, elementCount));
			}
		} catch (Throwable cause) {
			_logger.warn("could not prefetch element-count attribute", cause);
		}
	}
}
