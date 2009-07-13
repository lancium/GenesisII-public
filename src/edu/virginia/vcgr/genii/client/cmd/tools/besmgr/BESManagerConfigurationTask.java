package edu.virginia.vcgr.genii.client.cmd.tools.besmgr;

import org.ggf.bes.management.StartAcceptingNewActivitiesType;
import org.ggf.bes.management.StopAcceptingNewActivitiesType;
import org.morgan.util.gui.progress.ProgressNotifier;
import org.morgan.util.gui.progress.ProgressTask;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.bes.GeniiBESPortType;
import edu.virginia.vcgr.genii.client.bes.BESRP;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyManager;

public class BESManagerConfigurationTask implements
		ProgressTask<ManagementData>
{
	private ICallingContext _callingContext;
	private EndpointReferenceType _target;
	private ManagementData _data;
	
	BESManagerConfigurationTask(ICallingContext callingContext,
		EndpointReferenceType target, ManagementData data)
	{
		_callingContext = callingContext;
		_target = target;
		_data = data;
	}
	
	@Override
	public ManagementData compute(ProgressNotifier notifier) throws Exception
	{
		notifier.updateNote("Communicating with target BES container.");
		
		BESRP rp = (BESRP)ResourcePropertyManager.createRPInterface(
			_callingContext, _target,
			new Class<?> [] { BESRP.class},
			BESRP.IS_ACCEPTING_NEW_ACTIVITIES_ATTR,
			BESRP.POLICY_RP, BESRP.THRESHOLD_RP);
		
		rp.setPolicy(_data.policy());
		rp.setThreshold(_data.threshold());
		GeniiBESPortType bes = ClientUtils.createProxy(
			GeniiBESPortType.class, _target, _callingContext);
		if (_data.isAcceptingActivities())
			bes.startAcceptingNewActivities(new StartAcceptingNewActivitiesType());
		else
			bes.stopAcceptingNewActivities(new StopAcceptingNewActivitiesType());
		
		return new ManagementData(rp.getPolicy(), rp.getThreshold(),
			rp.isAcceptingNewActivities());
	}

	@Override
	public int getMaximumProgressValue()
	{
		// Shouldn't be called
		return 0;
	}

	@Override
	public int getMinimumProgressValue()
	{
		// Shouldn't be called
		return 100;
	}

	@Override
	public boolean isProgressIndeterminate()
	{
		return true;
	}
}