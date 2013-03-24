package edu.virginia.vcgr.genii.client.cmd.tools.besmgr;

import org.morgan.util.gui.progress.ProgressNotifier;
import org.morgan.util.gui.progress.ProgressTask;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.bes.BESRP;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyManager;

public class BESManagerAcquisitionTask implements ProgressTask<ManagementData>
{
	private ICallingContext _callingContext;
	private EndpointReferenceType _target;

	BESManagerAcquisitionTask(ICallingContext callingContext, EndpointReferenceType target)
	{
		_callingContext = callingContext;
		_target = target;
	}

	@Override
	public ManagementData compute(ProgressNotifier notifier) throws Exception
	{
		notifier.updateNote("Communicating with target BES container.");

		BESRP rp = (BESRP) ResourcePropertyManager.createRPInterface(_callingContext, _target, new Class<?>[] { BESRP.class },
			BESRP.IS_ACCEPTING_NEW_ACTIVITIES_ATTR, BESRP.POLICY_RP, BESRP.THRESHOLD_RP);

		return new ManagementData(rp.getPolicy(), rp.getThreshold(), rp.isAcceptingNewActivities());
	}

	@Override
	public int getMaximumProgressValue()
	{
		// Shouldn't really be called.
		return 0;
	}

	@Override
	public int getMinimumProgressValue()
	{
		// Shouldn't really be called.
		return 100;
	}

	@Override
	public boolean isProgressIndeterminate()
	{
		return true;
	}
}