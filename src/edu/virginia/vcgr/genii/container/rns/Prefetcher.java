package edu.virginia.vcgr.genii.container.rns;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.soap.SOAPException;

import org.apache.axis.message.MessageElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.cache.unified.CacheUtils;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.container.attrs.AttributePreFetcher;
import edu.virginia.vcgr.genii.container.common.AttributesPreFetcherFactory;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkService;

public class Prefetcher
{
	static private Log _logger = LogFactory.getLog(Prefetcher.class);

	public static MessageElement[] preFetch(EndpointReferenceType target, MessageElement[] existingAttributes,
		AttributesPreFetcherFactory factory, ResourceKey rKey, ResourceForkService service, boolean shortForm)
	{

		AttributePreFetcher preFetcher = null;
		MessageElement[] newattrs = null;

		// --------------------------------------------------------------------------------------------
		// temporary code to be removed by Prof. once he has done the necessary updates on
		// the server side to send EPR less responses. This temporary code does not have null
		// checking
		// that Prof. should have in his implementation ... and so on and so forth.
		// --------------------------------------------------------------------------------------------
		if (shortForm) {
			List<MessageElement> attributeList = new ArrayList<MessageElement>();
			TypeInformation type = new TypeInformation(target);
			StringBuilder buffer = new StringBuilder();
			if (type.isRNS())
				buffer.append("-RNS-");
			if (type.isByteIO())
				buffer.append("-ByteIO-");
			attributeList.add(new MessageElement(GenesisIIConstants.HUMAN_READABLE_PORT_TYPES_QNAME, buffer.toString()));
			WSName wsName = new WSName(target);
			MessageElement URIid = new MessageElement();
			URIid.setQName(GenesisIIConstants.RESOURCE_URI_QNAME);
			try {
				URIid.setObjectValue(wsName.getEndpointIdentifier());
			} catch (SOAPException e) {
				_logger.debug("could not set URI property in RNSEntryResponse: " + e.getMessage());
			}
			attributeList.add(URIid);
			attributeList.add(new MessageElement(GenesisIIConstants.CONTAINER_ID_QNAME, CacheUtils.getContainerId(target)));
			// ---------------------------------------------------------------------------------------------
			if (existingAttributes != null) {
				for (MessageElement attr : existingAttributes) {
					attributeList.add(attr);
				}
			}
			newattrs = attributeList.toArray(new MessageElement[attributeList.size()]);
		} else
			newattrs = existingAttributes;

		if (factory == null)
			return newattrs;

		try {
			preFetcher = factory.getPreFetcher(target, rKey, service);
			if (preFetcher == null)
				return newattrs;
			Collection<MessageElement> attrs = preFetcher.preFetch();
			if (attrs == null)
				return newattrs;

			if (newattrs != null) {
				for (MessageElement element : newattrs)
					attrs.add(element);
			}

			return attrs.toArray(new MessageElement[attrs.size()]);
		} catch (Throwable cause) {
			_logger.warn("Unable to pre-fetch attributes.", cause);
		} finally {
			if (preFetcher != null && (preFetcher instanceof Closeable))
				StreamUtils.close((Closeable) preFetcher);
		}

		return existingAttributes;
	}

}
