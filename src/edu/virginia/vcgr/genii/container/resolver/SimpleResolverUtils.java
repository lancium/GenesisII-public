package edu.virginia.vcgr.genii.container.resolver;

import java.net.URI;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.docs.wsrf.rl_2.Destroy;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.DefaultSubscriptionFactory;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.SubscribeException;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.Subscription;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.SubscriptionFactory;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.GenesisIIBaseTopics;
import edu.virginia.vcgr.genii.common.GeniiCommon;

public class SimpleResolverUtils
{
	static public String SIMPLE_RESOLVER_QNAME = "http://vcgr.cs.virginia.edu/naming/2006/12/simple-resolver";
	static public String SIMPLE_RESOLVER_EPI_LNAME = "EPI";
	static public String SIMPLE_RESOLVER_VERSION_LNAME = "Version";
	static public String SIMPLE_RESOLVER_GUID_LNAME = "GUID";
		
	static public QName REFERENCE_RESOLVER_EPI_QNAME = new QName(SIMPLE_RESOLVER_QNAME, SIMPLE_RESOLVER_EPI_LNAME);
	static public QName REFERENCE_RESOLVER_VERSION_QNAME = new QName(SIMPLE_RESOLVER_QNAME, SIMPLE_RESOLVER_VERSION_LNAME);
	static public QName REFERENCE_RESOLVER_GUID_QNAME = new QName(SIMPLE_RESOLVER_QNAME, SIMPLE_RESOLVER_GUID_LNAME);
	static private Log _logger = LogFactory.getLog(SimpleResolverUtils.class);
	
	/**
	 * Generates a new EPR that contains original the EPR (found in SimpleResolverEntry)
	 * plus ReferenceResolver element added to the end of the metadata element.  
	 * 
	 * @param targetEPR EndpointReferenceType.  Describes original EPR for target.
	 * @param resolverAddress AttributedURITypeSmart.  Address of resolver.
	 * @return EndpointReferenceType.  New EPR with resolver information embedded.
	 */
	static public EndpointReferenceType createResolutionEPR(EndpointReferenceType targetEPR, 
			EndpointReferenceType resolverEPR)
	{
		org.ws.addressing.AttributedURIType origAddress = targetEPR.getAddress();
		org.ws.addressing.ReferenceParametersType origRefParams = targetEPR.getReferenceParameters();
		org.ws.addressing.MetadataType origMetadata = targetEPR.getMetadata();
		org.apache.axis.message.MessageElement [] origMessageElements = targetEPR.get_any();
		
		org.ws.addressing.MetadataType newMetadata = null;
		org.apache.axis.message.MessageElement newResolverElement = new org.apache.axis.message.MessageElement(WSName.REFERENCE_RESOLVER_QNAME, resolverEPR);
		
		if (origMetadata == null)
		{
			origMetadata = new org.ws.addressing.MetadataType();
		}

		int numMetadataElements = 0;
		org.apache.axis.message.MessageElement [] origMetadataElements = origMetadata.get_any();
		if (origMetadataElements != null)
		{
			numMetadataElements = origMetadataElements.length;
		}
		
		org.apache.axis.message.MessageElement [] newMetadataElements = new org.apache.axis.message.MessageElement[numMetadataElements+1];
		
		for (int i = 0; i < numMetadataElements; i++)
		{
			newMetadataElements[i] = origMetadataElements[i];
		}
		newMetadataElements[numMetadataElements] = newResolverElement;
		newMetadata = new org.ws.addressing.MetadataType(newMetadataElements);
		
		EndpointReferenceType newEPR = new EndpointReferenceType(origAddress, origRefParams, newMetadata, origMessageElements);
		
		return newEPR;
	}
	
	static public EndpointReferenceType createTerminateSubscription(
		SimpleResolverEntry entry, EndpointReferenceType resolverEPR)
	{
		EndpointReferenceType newSubscriptionEPR = null;
		
		try
		{
			SubscriptionFactory factory = new DefaultSubscriptionFactory(
				resolverEPR);
			SimpleResolverTerminateUserData userData = 
				createTerminateSubscriptionUserData(entry);
			Subscription subscription = factory.subscribe(
				entry.getTargetEPR(),
				GenesisIIBaseTopics.RESOURCE_TERMINATION_TOPIC.asConcreteQueryExpression(),
				null, userData);
			newSubscriptionEPR = subscription.subscriptionReference();
		}
		catch (SubscribeException e)
		{ 
			_logger.debug("Could not create subscription to resource termination.", e);
		}
		
		return newSubscriptionEPR;
	}

	static public void terminateSubscription(EndpointReferenceType subscriptionEPR)
	{
		try
		{
			/* call terminate operation on subscription. */
			GeniiCommon subscription = ClientUtils.createProxy(GeniiCommon.class, subscriptionEPR);
			subscription.destroy(new Destroy());
		}
		catch (Exception e)
		{ 
			_logger.debug("Could not terminate subscription.", e);
		}
	}
	
	static protected SimpleResolverTerminateUserData
		createTerminateSubscriptionUserData(SimpleResolverEntry entry)
	{
		return new SimpleResolverTerminateUserData(
			URI.create(entry.getTargetEPI().toString()),
			entry.getVersion(),
			entry.getSubscriptionGUID());
	}
}