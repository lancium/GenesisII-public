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

public class CloudExecutePhase  extends AbstractCloudExecutionPhase 
implements Serializable{

	static final long serialVersionUID = 0L;

	private String _runScript;

	static private Log _logger = LogFactory.getLog(CloudExecutePhase.class);

	public CloudExecutePhase(String activityID, String besid,
			String workingDir, String runScript){
		_activityID = activityID;
		_besid = besid;
		_workingDir = workingDir;
		_runScript = runScript;
	}

	@Override
	public ActivityState getPhaseState() {
		return new ActivityState(ActivityStateEnumeration.Running,
				"Sending Execution Command", false);
	}

	@Override
	public void execute(ExecutionContext context) throws Throwable {
		CloudManager tManage = CloudMonitor.getManager(_besid);
		String resourceID  = tManage.aquireResource(_activityID);

		HistoryContext history = HistoryContextFactory.createContext(
				HistoryEventCategory.Default);

		history.createInfoWriter("Sending Execute Command").close();
		
		//Build Command (nohup and set to background)
		String command =
			"nohup " + _workingDir + _runScript + " &> /dev/null &";
		tryExecuteCommand(resourceID, command,
				System.out, System.err, tManage);
		_logger.info("CloudBES: Activity " + _activityID +
				" Sent Execution Command");

	}

	@Override
	protected Log getLog() {
		return _logger;
	}

	@Override
	protected String getPhase() {
		return "Send Execution Command";
	}

}
