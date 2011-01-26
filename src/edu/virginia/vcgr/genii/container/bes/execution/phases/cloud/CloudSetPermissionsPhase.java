package edu.virginia.vcgr.genii.container.bes.execution.phases.cloud;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.factory.ActivityStateEnumeration;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.cloud.CloudManager;
import edu.virginia.vcgr.genii.cloud.CloudMonitor;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;

public class CloudSetPermissionsPhase extends AbstractCloudExecutionPhase 
	implements Serializable{

	static final long serialVersionUID = 0L;

	private ArrayList<String> _files = new ArrayList<String>();

	static private Log _logger = LogFactory.getLog(CloudSetPermissionsPhase.class);

	public CloudSetPermissionsPhase(String besid, String activityID,
			Collection<String> files){
		_besid = besid;
		_activityID = activityID;
		_files.addAll(files);
	}


	@Override
	public ActivityState getPhaseState() {
		return new ActivityState(ActivityStateEnumeration.Running,
				"setting-permissions", false);
	}

	@Override
	public void execute(ExecutionContext context) throws Throwable {

		CloudManager tManage = CloudMonitor.getManager(_besid);
		String resourceID  = tManage.aquireResource(_activityID);

		//Build Command
		String command = "";
		for (String tFile : _files){
			command +=  "chmod +x " + tFile + "; ";
		}


		tryExecuteCommand(resourceID, command, System.out, System.err, tManage);
		_logger.info("CloudBES: Activity " + _activityID +
				" Set Permissions in VM");

	}


	@Override
	protected Log getLog() {
		return _logger;
	}


	@Override
	protected String getPhase() {
		return "Setting Permissions";
	}

}
