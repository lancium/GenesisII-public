package edu.virginia.vcgr.genii.container.bes.execution.phases.cloud;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import org.ggf.bes.factory.ActivityStateEnumeration;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.cloud.CloudManager;
import edu.virginia.vcgr.genii.cloud.CloudMonitor;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionPhase;

public class CloudSetPermissionsPhase implements ExecutionPhase, Serializable{

	static final long serialVersionUID = 0L;
	private String _besid;
	private String _activityID;
	private ArrayList<String> _files = new ArrayList<String>();
	
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
		
		tManage.sendCommand(resourceID, command, System.out, System.err);
		
	}

}
