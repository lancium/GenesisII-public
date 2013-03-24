package edu.virginia.vcgr.genii.container.bes.execution.phases.cloud;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.factory.ActivityStateEnumeration;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.history.HistoryEventCategory;
import edu.virginia.vcgr.genii.cloud.CloudManager;
import edu.virginia.vcgr.genii.cloud.CloudMonitor;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionPhase;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContext;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContextFactory;

public class CloudGetResourcePhase implements ExecutionPhase, Serializable
{

	static final long serialVersionUID = 0L;

	private String _activityID;
	private String _besid;

	static private Log _logger = LogFactory.getLog(CloudGetResourcePhase.class);

	public CloudGetResourcePhase(String activityID, String besid)
	{
		_activityID = activityID;
		_besid = besid;
	}

	@Override
	public ActivityState getPhaseState()
	{
		return new ActivityState(ActivityStateEnumeration.Running, "acquiring-resource", false);
	}

	@Override
	public void execute(ExecutionContext context) throws Throwable
	{
		HistoryContext history = HistoryContextFactory.createContext(HistoryEventCategory.Scheduling);

		history.createInfoWriter("Requesting Cloud Resource").close();

		CloudManager tManage = CloudMonitor.getManager(_besid);
		String resourceID = null;
		if (tManage != null)
			resourceID = tManage.aquireResource(_activityID);

		if (resourceID != null) {
			_logger.info("CloudBES: Activity " + _activityID + " aquired resource " + resourceID);
			history.createInfoWriter("Activity " + _activityID + " aquired resource " + resourceID).close();
		}
	}

}