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
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXMapping;
import edu.virginia.vcgr.genii.client.wsrf.WSRFConstants;
import edu.virginia.vcgr.genii.common.notification.GeniiPullPointPortType;
import edu.virginia.vcgr.genii.container.common.GenesisIIBase;
import edu.virginia.vcgr.genii.container.configuration.GeniiServiceConfiguration;
import edu.virginia.vcgr.genii.container.util.FaultManipulator;

@GeniiServiceConfiguration(
	resourceProvider=DBPullPointResourceProvider.class)
public class GeniiPullPointServiceImpl extends GenesisIIBase
	implements GeniiPullPointPortType
{
	public GeniiPullPointServiceImpl() throws RemoteException
	{
		super("GeniiPullPointPortType");
		
		addImplementedPortType(WSRFConstants.WSN_PULL_POINT_PORT);
	}

	@Override
	public PortType getFinalWSResourceInterface()
	{
		return WellKnownPortTypes.GENII_PULL_POINT_PORT;
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public DestroyPullPointResponse destroyPullPoint(DestroyPullPoint arg0)
			throws RemoteException, UnableToDestroyPullPointFaultType,
			ResourceUnknownFaultType
	{
		super.destroy(new Destroy());
		return new DestroyPullPointResponse();
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public GetMessagesResponse getMessages(GetMessages arg0)
			throws RemoteException, UnableToGetMessagesFaultType,
			ResourceUnknownFaultType
	{
		throw FaultManipulator.fillInFault(new UnableToGetMessagesFaultType());
	}
}
