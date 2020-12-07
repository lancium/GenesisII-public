package edu.virginia.vcgr.genii.container.common.notification;

import java.rmi.RemoteException;

import org.oasis_open.docs.wsrf.r_2.ResourceUnknownFaultType;
import org.oasis_open.docs.wsrf.rl_2.Destroy;
import org.oasis_open.wsn.base.DestroyPullPoint;
import org.oasis_open.wsn.base.DestroyPullPointResponse;
import org.oasis_open.wsn.base.GetMessages;
import org.oasis_open.wsn.base.GetMessagesResponse;
import org.oasis_open.wsn.base.UnableToDestroyPullPointFaultType;
import org.oasis_open.wsn.base.UnableToGetMessagesFaultType;

import edu.virginia.vcgr.genii.client.WellKnownPortTypes;
import edu.virginia.vcgr.genii.client.resource.PortType;
import edu.virginia.vcgr.genii.client.wsrf.FaultManipulator;
import edu.virginia.vcgr.genii.client.wsrf.WSRFConstants;
import edu.virginia.vcgr.genii.common.notification.GeniiPullPointPortType;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.configuration.GeniiServiceConfiguration;
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.rwx.RWXMapping;

@GeniiServiceConfiguration(resourceProvider = DBPullPointResourceProvider.class)
public class GeniiPullPointServiceImpl extends GenesisIIBase implements GeniiPullPointPortType
{
	
	// 2020-12-1 by ASG
	// keyInEPR is intended as a replacement for instanceof(GeniiNoOutcalls) which was a bit hacky.
	// If it is "true", we will not put key material in the X.509. This will in turn prevent delegation to instances
	// of a type that returns true, and will make transporting and storing EPR's consume MUCH less space.
	public boolean keyInEPR() {
		return false;
	}
	
	public GeniiPullPointServiceImpl() throws RemoteException
	{
		super("GeniiPullPointPortType");

		addImplementedPortType(WSRFConstants.WSN_PULL_POINT_PORT());
	}

	@Override
	public PortType getFinalWSResourceInterface()
	{
		return WellKnownPortTypes.GENII_PULL_POINT_PORT();
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public DestroyPullPointResponse destroyPullPoint(DestroyPullPoint arg0)
		throws RemoteException, UnableToDestroyPullPointFaultType, ResourceUnknownFaultType
	{
		super.destroy(new Destroy());
		return new DestroyPullPointResponse();
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public GetMessagesResponse getMessages(GetMessages arg0) throws RemoteException, UnableToGetMessagesFaultType, ResourceUnknownFaultType
	{
		throw FaultManipulator.fillInFault(new UnableToGetMessagesFaultType());
	}
}
