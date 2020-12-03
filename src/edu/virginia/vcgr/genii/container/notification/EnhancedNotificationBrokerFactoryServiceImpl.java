package edu.virginia.vcgr.genii.container.notification;

import java.rmi.RemoteException;
import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.wsrf.basefaults.BaseFaultTypeDescription;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.comm.axis.Elementals;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.Container;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.notification.factory.BrokerWithForwardingPortCreateRequestType;
import edu.virginia.vcgr.genii.notification.factory.EnhancedNotificationBrokerFactoryPortType;
import edu.virginia.vcgr.genii.notification.factory.NotificationBrokerCreationFailedFaultType;
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.rwx.RWXMapping;

public class EnhancedNotificationBrokerFactoryServiceImpl extends GenesisIIBase implements EnhancedNotificationBrokerFactoryPortType
{

	public static final String SERVICE_URL = "EnhancedNotificationBrokerFactoryPortType";

	private static Log _logger = LogFactory.getLog(EnhancedNotificationBrokerFactoryServiceImpl.class);

	// 2020-12-1 by ASG
	// keyInEPR is intended as a replacement for instanceof(GeniiNoOutcalls) which was a bit hacky.
	// If it is "true", we will not put key material in the X.509. This will in turn prevent delegation to instances
	// of a type that returns true, and will make transporting and storing EPR's consume MUCH less space.
	public boolean keyInEPR() {
		return false;
	}
	
	public EnhancedNotificationBrokerFactoryServiceImpl() throws RemoteException
	{
		super(SERVICE_URL);
	}

	public EnhancedNotificationBrokerFactoryServiceImpl(String serviceName) throws RemoteException
	{
		super(SERVICE_URL);
	}

	@RWXMapping(RWXCategory.EXECUTE)
	@Override
	public EndpointReferenceType createNotificationBroker(long lifeTimeOfBroker)
		throws RemoteException, NotificationBrokerCreationFailedFaultType
	{

		NotificationBrokerConstructionParams cParams = new NotificationBrokerConstructionParams();
		cParams.timeToLive(lifeTimeOfBroker);
		cParams.setScheduledTerminationTime(lifeTimeOfBroker);
		cParams.setMode(false);
		try {
			EndpointReferenceType brokerEndpoint =
				new EnhancedNotificationBrokerServiceImpl().CreateEPR(Elementals.unitaryArray(cParams.serializeToMessageElement()),
					Container.getServiceURL(EnhancedNotificationBrokerServiceImpl.PORT_NAME));
			_logger.info("notification broker is created without any forwarding port");
			return brokerEndpoint;

		} catch (Exception ex) {
			final NotificationBrokerCreationFailedFaultType fault =
				new NotificationBrokerCreationFailedFaultType(null, Calendar.getInstance(), null, null,
					new BaseFaultTypeDescription[] { new BaseFaultTypeDescription("Unable to create notification broker.") }, null, null);
			throw fault;
		}
	}

	@RWXMapping(RWXCategory.EXECUTE)
	@Override
	public EndpointReferenceType createNotificationBrokerWithForwardingPort(BrokerWithForwardingPortCreateRequestType request)
		throws RemoteException, NotificationBrokerCreationFailedFaultType
	{

		NotificationBrokerConstructionParams cParams = new NotificationBrokerConstructionParams();
		final long lifetime = request.getNotificationBrokerLifetime();
		cParams.timeToLive(lifetime);
		cParams.setScheduledTerminationTime(lifetime);
		cParams.setMode(false);
		cParams.setForwardingPort(request.getNotificationForwardingPort());
		try {
			EndpointReferenceType brokerEndpoint =
				new EnhancedNotificationBrokerServiceImpl().CreateEPR(Elementals.unitaryArray(cParams.serializeToMessageElement()),
					Container.getServiceURL(EnhancedNotificationBrokerServiceImpl.PORT_NAME));

			_logger.info("notification broker is created with a forwarding port.");
			return brokerEndpoint;

		} catch (Exception ex) {
			final NotificationBrokerCreationFailedFaultType fault =
				new NotificationBrokerCreationFailedFaultType(null, Calendar.getInstance(), null, null,
					new BaseFaultTypeDescription[] { new BaseFaultTypeDescription("Unable to create notification broker.") }, null, null);
			throw fault;
		}
	}

	@Override
	protected boolean allowVcgrCreate() throws ResourceException, ResourceUnknownFaultType
	{
		return false;
	}

	public PortType getFinalWSResourceInterface()
	{
		return WellKnownPortTypes.ENHANCED_NOTIFICATION_BROKER_FACTORY__PORT();
	}
}
