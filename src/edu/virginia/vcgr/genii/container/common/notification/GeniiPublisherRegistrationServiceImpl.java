package edu.virginia.vcgr.genii.container.common.notification;

import java.rmi.RemoteException;

import org.oasis_open.docs.wsn.br_2.DestroyRegistration;
import org.oasis_open.docs.wsn.br_2.DestroyRegistrationResponse;
import org.oasis_open.docs.wsn.br_2.ResourceNotDestroyedFaultType;
import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.docs.wsrf.rl_2.Destroy;

import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.wsrf.WSRFConstants;
import edu.virginia.vcgr.genii.common.notification.GeniiPublisherRegistrationPortType;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.configuration.GeniiServiceConfiguration;
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.rwx.RWXMapping;

@GeniiServiceConfiguration(resourceProvider = DBPublisherRegistrationResourceProvider.class)
public class GeniiPublisherRegistrationServiceImpl extends GenesisIIBase implements GeniiPublisherRegistrationPortType
{
	public GeniiPublisherRegistrationServiceImpl() throws RemoteException
	{
		super("GeniiPublisherRegistrationPortType");

		addImplementedPortType(WSRFConstants.WSN_PUBLISHER_REGISTRATION_MANAGER_PORT);
	}

	@Override
	public PortType getFinalWSResourceInterface()
	{
		return WellKnownPortTypes.GENII_PUB_REG_MGR_PORT_TYPE;
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public DestroyRegistrationResponse destroyRegistration(DestroyRegistration arg0) throws RemoteException,
		ResourceUnknownFaultType, ResourceNotDestroyedFaultType
	{
		destroy(new Destroy());
		return new DestroyRegistrationResponse();
	}
}