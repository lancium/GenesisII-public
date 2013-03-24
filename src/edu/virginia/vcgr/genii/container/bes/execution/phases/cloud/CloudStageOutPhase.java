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
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContext;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContextFactory;

public class CloudStageOutPhase extends AbstractCloudExecutionPhase implements Serializable
{

	static final long serialVersionUID = 0L;

	private String _stageScript;

	static private Log _logger = LogFactory.getLog(CloudStageOutPhase.class);

	public CloudStageOutPhase(String activityID, String besid, String workingDir, String stageScript)
	{
		_activityID = activityID;
		_besid = besid;
		_workingDir = workingDir;
		_stageScript = stageScript;
	}

	@Override
	public ActivityState getPhaseState()
	{
		return new ActivityState(ActivityStateEnumeration.Running, "staging-out", false);
	}

	@Override
	public void execute(ExecutionContext context) throws Throwable
	{

		HistoryContext history = HistoryContextFactory.createContext(HistoryEventCategory.StageOut);

		history.createInfoWriter("Sending Stage out command").close();

		CloudManager tManage = CloudMonitor.getManager(_besid);
		String resourceID = tManage.aquireResource(_activityID);

		// Build Command (nohup and set to background)
		String command = "nohup " + _workingDir + _stageScript + " &> /dev/null &";

		tryExecuteCommand(resourceID, command, System.out, System.err, tManage);
		_logger.info("CloudBES: Activity " + _activityID + " Sent Stage Out Command");
	}

	@Override
	protected Log getLog()
	{
		return _logger;
	}

	@Override
	protected String getPhase()
	{
		return "Stage-Out";
	}

}
