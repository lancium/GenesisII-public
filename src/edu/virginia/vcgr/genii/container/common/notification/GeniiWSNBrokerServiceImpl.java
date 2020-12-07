package edu.virginia.vcgr.genii.container.common.notification;

import java.rmi.RemoteException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.docs.wsn.br_2.PublisherRegistrationFailedFaultType;
import org.oasis_open.docs.wsn.br_2.PublisherRegistrationRejectedFaultType;
import org.oasis_open.docs.wsn.br_2.RegisterPublisher;
import org.oasis_open.docs.wsn.br_2.RegisterPublisherResponse;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.wsn.base.CreatePullPointResponse;
import org.oasis_open.wsn.base.CreatePullPoint_Element;
import org.oasis_open.wsn.base.InvalidTopicExpressionFaultType;
import org.oasis_open.wsn.base.TopicNotSupportedFaultType;
import org.oasis_open.wsn.base.UnableToCreatePullPointFaultType;
import org.oasis_open.wsn.base.UnacceptableInitialTerminationTimeFaultType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.comm.attachments.GeniiAttachment;
import edu.virginia.vcgr.genii.client.notification.NotificationConstants;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.wsrf.FaultManipulator;
import edu.virginia.vcgr.genii.client.wsrf.WSRFConstants;
import edu.virginia.vcgr.genii.client.wsrf.wsn.AbstractNotificationHandler;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMessageContents;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMultiplexer;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;
import edu.virginia.vcgr.genii.common.notification.GeniiWSNBrokerPortType;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.configuration.GeniiServiceConfiguration;
import edu.virginia.vcgr.genii.container.cservices.ContainerServices;
import edu.virginia.vcgr.genii.container.cservices.wsn.WSNotificationContainerService;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.rwx.RWXMapping;

@GeniiServiceConfiguration(resourceProvider = DBBrokerResourceProvider.class)
public class GeniiWSNBrokerServiceImpl extends GenesisIIBase implements GeniiWSNBrokerPortType
{
	@SuppressWarnings("unused")
	static private Log _logger = LogFactory.getLog(GeniiWSNBrokerServiceImpl.class);
	
	// 2020-12-1 by ASG
	// keyInEPR is intended as a replacement for instanceof(GeniiNoOutcalls) which was a bit hacky.
	// If it is "true", we will not put key material in the X.509. This will in turn prevent delegation to instances
	// of a type that returns true, and will make transporting and storing EPR's consume MUCH less space.
	public boolean keyInEPR() {
		return true;
	}

	private class BrokeredNotificationHandler extends AbstractNotificationHandler<NotificationMessageContents>
	{
		private BrokeredNotificationHandler()
		{
			super(NotificationMessageContents.class);
		}

		@Override
		final public String handleNotification(TopicPath topic, EndpointReferenceType producerReference,
			EndpointReferenceType subscriptionReference, NotificationMessageContents contents) throws Exception
		{
			// Should we broker attachments?
			GeniiAttachment attachment = null;

			ResourceKey rKey = ResourceManager.getCurrentResource();
			WSNotificationContainerService wsnService = ContainerServices.findService(WSNotificationContainerService.class);

			wsnService.publishNotification(rKey.getResourceKey(), producerReference, topic, contents, attachment);
			return NotificationConstants.OK;
		}
	}

	@Override
	protected void registerNotificationHandlers(NotificationMultiplexer multiplexer)
	{
		super.registerNotificationHandlers(multiplexer);

		multiplexer.registerNotificationHandler(null, new BrokeredNotificationHandler());
	}

	public GeniiWSNBrokerServiceImpl() throws RemoteException
	{
		super("GeniiWSNBrokerPortType");

		addImplementedPortType(WSRFConstants.WSN_CREATE_PULL_POINT_PORT());
		addImplementedPortType(WSRFConstants.WSN_BROKERED_NOTIFICATION_PORT());
		addImplementedPortType(WSRFConstants.WSN_REGISTER_PUBLISHER_PORT());
	}

	@Override
	public PortType getFinalWSResourceInterface()
	{
		return WellKnownPortTypes.GENII_WSNBROKER_PORT_TYPE();
	}

	@Override
	@RWXMapping(RWXCategory.EXECUTE)
	public CreatePullPointResponse createPullPoint(CreatePullPoint_Element arg0) throws RemoteException, UnableToCreatePullPointFaultType
	{
		throw FaultManipulator.fillInFault(new UnableToCreatePullPointFaultType());
	}

	@Override
	@RWXMapping(RWXCategory.EXECUTE)
	public RegisterPublisherResponse registerPublisher(RegisterPublisher arg0) throws RemoteException, PublisherRegistrationRejectedFaultType,
		TopicNotSupportedFaultType, UnacceptableInitialTerminationTimeFaultType, InvalidTopicExpressionFaultType, ResourceUnknownFaultType,
		PublisherRegistrationFailedFaultType
	{
		throw FaultManipulator.fillInFault(new PublisherRegistrationRejectedFaultType());
	}
}