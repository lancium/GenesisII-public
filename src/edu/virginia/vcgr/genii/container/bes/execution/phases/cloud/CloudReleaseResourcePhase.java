package edu.virginia.vcgr.genii.container.bes.execution.phases.cloud;

import java.io.Serializable;

import org.ggf.bes.factory.ActivityStateEnumeration;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.bes.ExecutionContext;
import edu.virginia.vcgr.genii.container.bes.ExecutionPhase;
import edu.virginia.vcgr.genii.client.history.HistoryEventCategory;
import edu.virginia.vcgr.genii.cloud.CloudManager;
import edu.virginia.vcgr.genii.cloud.CloudMonitor;
import edu.virginia.vcgr.genii.container.bes.activity.BESActivity;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContext;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContextFactory;

public class CloudReleaseResourcePhase implements ExecutionPhase, Serializable
{

	static final long serialVersionUID = 0L;

	private String _activityID;
	private String _besid;

	public CloudReleaseResourcePhase(String activityID, String besid)
	{
		_activityID = activityID;
		_besid = besid;
	}

	@Override
	public ActivityState getPhaseState()
	{
		return new ActivityState(ActivityStateEnumeration.Running, "releasing-resource");
	}

	@Override
	public void execute(ExecutionContext context, BESActivity activity) throws Throwable
	{
		
		HistoryContext history = HistoryContextFactory.createContext(HistoryEventCategory.Cleanup);

		history.createInfoWriter("Releasing Cloud Resources").close();

		CloudManager tManage = CloudMonitor.getManager(_besid);
		if (tManage != null)
			tManage.releaseResource(_activityID);

	}

}
