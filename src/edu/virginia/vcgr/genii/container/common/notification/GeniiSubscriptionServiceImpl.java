package edu.virginia.vcgr.genii.container.common.notification;

import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.axis.message.MessageElement;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.docs.wsrf.rl_2.Destroy;
import org.oasis_open.wsn.base.AbsoluteOrRelativeTimeType;
import org.oasis_open.wsn.base.PauseFailedFaultType;
import org.oasis_open.wsn.base.PauseSubscription;
import org.oasis_open.wsn.base.PauseSubscriptionResponse;
import org.oasis_open.wsn.base.Renew;
import org.oasis_open.wsn.base.RenewResponse;
import org.oasis_open.wsn.base.ResumeFailedFaultType;
import org.oasis_open.wsn.base.ResumeSubscription;
import org.oasis_open.wsn.base.ResumeSubscriptionResponse;
import org.oasis_open.wsn.base.UnableToDestroySubscriptionFaultType;
import org.oasis_open.wsn.base.UnacceptableTerminationTimeFaultType;
import org.oasis_open.wsn.base.Unsubscribe;
import org.oasis_open.wsn.base.UnsubscribeResponse;
import org.oasis_open.wsn.base.UnsupportedPolicyRequestFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.common.ConstructionParameters;
import edu.virginia.vcgr.genii.client.common.ConstructionParametersType;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.wsrf.WSRFConstants;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.TerminationTimeType;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.policy.SubscriptionPolicy;
import edu.virginia.vcgr.genii.client.wsrf.wsn.subscribe.policy.SubscriptionPolicyTypes;
import edu.virginia.vcgr.genii.common.notification.GeniiSubscriptionPortType;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.configuration.GeniiServiceConfiguration;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.rwx.RWXMapping;

@ConstructionParametersType(SubscriptionConstructionParameters.class)
@GeniiServiceConfiguration(resourceProvider = DBSubscriptionResourceProvider.class)
public class GeniiSubscriptionServiceImpl extends GenesisIIBase implements GeniiSubscriptionPortType
{
	@Override
	protected void postCreate(ResourceKey rKey, EndpointReferenceType newEPR, ConstructionParameters cParams,
		HashMap<QName, Object> constructionParameters, Collection<MessageElement> resolverCreationParameters)
		throws ResourceException, BaseFaultType, RemoteException
	{
		super.postCreate(rKey, newEPR, cParams, constructionParameters, resolverCreationParameters);

		SubscriptionConstructionParameters sCons = (SubscriptionConstructionParameters) cParams;

		DBSubscriptionResource resource = (DBSubscriptionResource) rKey.dereference();

		Map<SubscriptionPolicyTypes, SubscriptionPolicy> policies = sCons.policies();

		if (policies != null) {
			if (policies.containsKey(SubscriptionPolicyTypes.UseRawPolicy))
				throw FaultManipulator.fillInFault(new UnsupportedPolicyRequestFaultType());
			if (policies.containsKey(SubscriptionPolicyTypes.BatchEvents))
				throw FaultManipulator.fillInFault(new UnsupportedPolicyRequestFaultType());
			if (policies.containsKey(SubscriptionPolicyTypes.CollapseEvents))
				throw FaultManipulator.fillInFault(new UnsupportedPolicyRequestFaultType());
			if (policies.containsKey(SubscriptionPolicyTypes.IgnoreDuplicateEvents))
				throw FaultManipulator.fillInFault(new UnsupportedPolicyRequestFaultType());
		}

		resource.createSubscription(newEPR, sCons);
	}

	public GeniiSubscriptionServiceImpl() throws RemoteException
	{
		super("GeniiSubscriptionPortType");

		addImplementedPortType(WSRFConstants.WSN_CREATE_PULL_POINT_PORT);
		addImplementedPortType(WSRFConstants.WSN_SUBSCRIPTION_MANAGER_PORT);
		addImplementedPortType(WSRFConstants.WSN_PAUSABLE_SUBSCRIPTION_MANAGER_PORT);
	}

	@Override
	public PortType getFinalWSResourceInterface()
	{
		return WellKnownPortTypes.GENII_SUBSCRIPTION_PORT_TYPE;
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public PauseSubscriptionResponse pauseSubscription(PauseSubscription arg0) throws RemoteException, PauseFailedFaultType,
		ResourceUnknownFaultType
	{
		DBSubscriptionResource resource = (DBSubscriptionResource) ResourceManager.getCurrentResource().dereference();
		resource.toggleSubscriptionPause(true);
		return new PauseSubscriptionResponse();
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public RenewResponse renew(Renew renew) throws RemoteException, ResourceUnknownFaultType,
		UnacceptableTerminationTimeFaultType
	{
		AbsoluteOrRelativeTimeType termTime = renew.getTerminationTime();
		if (termTime == null)
			throw FaultManipulator.fillInFault(new UnacceptableTerminationTimeFaultType());

		TerminationTimeType ttt = TerminationTimeType.newInstance(termTime);
		Calendar currentTime = Calendar.getInstance();
		Calendar terminationTime = ttt.terminationTime();
		setScheduledTerminationTime(terminationTime);
		return new RenewResponse(terminationTime, currentTime, null);
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public ResumeSubscriptionResponse resumeSubscription(ResumeSubscription arg0) throws RemoteException,
		ResourceUnknownFaultType, ResumeFailedFaultType
	{
		DBSubscriptionResource resource = (DBSubscriptionResource) ResourceManager.getCurrentResource().dereference();
		resource.toggleSubscriptionPause(false);
		return new ResumeSubscriptionResponse();
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public UnsubscribeResponse unsubscribe(Unsubscribe arg0) throws RemoteException, UnableToDestroySubscriptionFaultType,
		ResourceUnknownFaultType
	{
		super.destroy(new Destroy());

		return new UnsubscribeResponse();
	}
}