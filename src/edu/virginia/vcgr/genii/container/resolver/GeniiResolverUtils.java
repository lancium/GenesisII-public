package edu.virginia.vcgr.genii.container.resolver;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.axis.types.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.EndpointReferenceType;
import org.ws.addressing.ReferenceParametersType;

import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.naming.ResolverDescription;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.naming.ResolverDescription.ResolverType;
import edu.virginia.vcgr.genii.client.resource.AddressingParameters;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.DefaultSubscriptionFactory;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.Subscription;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.SubscriptionFactory;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.wellknown.GenesisIIBaseTopics;
import edu.virginia.vcgr.genii.container.context.WorkingContext;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.sync.SyncProperty;

public class GeniiResolverUtils
{
	static private Log _logger = LogFactory.getLog(GeniiResolverUtils.class);

	// Database keys for storing resolver state used by doResolve().
	static public final String NEXT_TARGET_ID_PROP_NAME = "edu.virginia.vcgr.genii.resolver.next-id";

	/**
	 * Generates a new EPR that is the given original EPR plus a Resolver element added to the the
	 * metadata element.
	 */
	static public EndpointReferenceType createResolutionEPR(IGeniiResolverResource resource, EndpointReferenceType targetEPR,
		EndpointReferenceType resolverEPR, int targetID) throws ResourceException
	{
		WSName targetName = new WSName(targetEPR);
		URI targetEPI = targetName.getEndpointIdentifier();
		List<ResolverDescription> resolvers = new ArrayList<ResolverDescription>();

		// The target EPR contains the resolver EPR (in case the target address is down),
		// and the resolver EPR contains the target EPR's ID (so that the resolver can select
		// another replica when the target EPR is down).
		resolverEPR = createUserInfoEPR(resolverEPR, GeniiResolverServiceImpl.GENII_RESOLVER_TARGET_ID_KEY, new Integer(
			targetID), true);
		resolvers.add(new ResolverDescription(targetEPI, resolverEPR, ResolverType.EPI_RESOLVER));

		Integer nextID = (Integer) resource.getProperty(NEXT_TARGET_ID_PROP_NAME);
		if (nextID != null) {
			URI resolverEPI = EPRUtils.extractEndpointIdentifier(resolverEPR);
			resolverEPR = resource.getTargetEPR(resolverEPI, nextID);
			resolverEPR = createUserInfoEPR(resolverEPR, GeniiResolverServiceImpl.GENII_RESOLVER_TARGET_ID_KEY, new Integer(
				targetID), true);
			resolvers.add(new ResolverDescription(targetEPI, resolverEPR, ResolverType.EPI_RESOLVER));
		}

		targetName.setResolvers(resolvers);
		return targetName.getEndpoint();
	}

	/**
	 * Subscribe to the "terminate" topic of the new EPR, so that the resolver will be notified if
	 * the new resource is destroyed.
	 */
	static public EndpointReferenceType createTerminateSubscription(int targetID, EndpointReferenceType targetEPR,
		EndpointReferenceType resolverEPR, IResource resource) throws ResourceException
	{
		// if (_logger.isDebugEnabled()) _logger.debug("resolver: createTerminateSubscription()");
		EndpointReferenceType subscriptionEPR = null;
		try {
			if (resolverEPR == null) {
				resolverEPR = (EndpointReferenceType) WorkingContext.getCurrentWorkingContext().getProperty(
					WorkingContext.EPR_PROPERTY_NAME);
			}
			SubscriptionFactory factory = new DefaultSubscriptionFactory(resolverEPR);
			WSName wsname = new WSName(targetEPR);
			URI targetEPI = wsname.getEndpointIdentifier();
			SimpleResolverTerminateUserData userData = new SimpleResolverTerminateUserData(targetEPI, targetID);
			Subscription subscription = factory.subscribe(targetEPR,
				GenesisIIBaseTopics.RESOURCE_TERMINATION_TOPIC.asConcreteQueryExpression(), null, userData);
			subscriptionEPR = subscription.subscriptionReference();
			// String subscriptionID = targetEPI + ":" + targetID;
			// resource.recordSubscription(subscriptionID, subscriptionEPR);
		} catch (Exception exception) {
			if (_logger.isDebugEnabled())
				_logger.debug("Could not create subscription to resource termination.", exception);
		}
		return subscriptionEPR;
	}

	/**
	 * Return a new EPR with the contents of the original EPR plus the given additionalUserInfo. The
	 * original EPR is unmodified. However, it shares data structures with the new EPR, so the new
	 * EPR should be treated as read-only.
	 */
	public static EndpointReferenceType createUserInfoEPR(EndpointReferenceType epr, String key, Serializable data,
		boolean doClear)
	{
		ReferenceParametersType refParams = epr.getReferenceParameters();
		try {
			AddressingParameters addParams = new AddressingParameters(refParams);
			Map<String, Serializable> userInfo = null;
			if (!doClear)
				userInfo = addParams.getAdditionalUserInformation();
			if (userInfo == null)
				userInfo = new TreeMap<String, Serializable>();
			userInfo.put(key, data);
			addParams.setAdditionalUserInformation(userInfo);
			refParams = addParams.toReferenceParameters();
		} catch (ResourceException exception) {
			// This should never happen.
			if (_logger.isDebugEnabled())
				_logger.debug("Could not create AddressingParameters.", exception);
		}
		return new EndpointReferenceType(epr.getAddress(), refParams, epr.getMetadata(), epr.get_any());
	}

	/**
	 * If this resolver is adding a resource that is a replica of itself, then it sets a property
	 * that indicates that when it returns its own EPR, it should include this resource as a
	 * resolver.
	 */
	static public int[] initializeNextTargetID(IGeniiResolverResource resource, URI targetEPI, int[] targetIDList)
		throws ResourceException
	{
		// Already initialized? Do nothing.
		if (targetIDList.length > 0)
			return targetIDList;
		// Not this EPI? Do nothing.
		EndpointReferenceType myEPR = (EndpointReferenceType) WorkingContext.getCurrentWorkingContext().getProperty(
			WorkingContext.EPR_PROPERTY_NAME);
		if (!targetEPI.equals(EPRUtils.extractEndpointIdentifier(myEPR)))
			return targetIDList;
		// Initialize.
		if (_logger.isDebugEnabled())
			_logger.debug("initialize resolver as replicated");
		int targetID = 0;
		resource.addTargetEPR(targetEPI, targetID, myEPR);
		resource.setProperty(SyncProperty.TARGET_ID_PROP_NAME, new Integer(targetID));
		targetIDList = new int[1];
		targetIDList[0] = targetID;
		return targetIDList;
	}

	static public void initializeNextTargetIDinReplica(IGeniiResolverResource resource) throws ResourceException
	{
		resource.setProperty(NEXT_TARGET_ID_PROP_NAME, new Integer(0));
	}

	static public void updateNextTargetID(IGeniiResolverResource resource, URI targetEPI, int targetID)
		throws ResourceException
	{
		// Not this EPI? Do nothing.
		EndpointReferenceType myEPR = (EndpointReferenceType) WorkingContext.getCurrentWorkingContext().getProperty(
			WorkingContext.EPR_PROPERTY_NAME);
		if (!targetEPI.equals(EPRUtils.extractEndpointIdentifier(myEPR)))
			return;
		Integer nextID = (Integer) resource.getProperty(NEXT_TARGET_ID_PROP_NAME);
		if ((nextID == null) || (nextID.intValue() == 0)) {
			Integer currentID = (Integer) resource.getProperty(SyncProperty.TARGET_ID_PROP_NAME);
			if (_logger.isDebugEnabled())
				_logger.debug("resolver " + currentID + ": next=" + targetID);
			resource.setProperty(NEXT_TARGET_ID_PROP_NAME, new Integer(targetID));
		}
	}
}
