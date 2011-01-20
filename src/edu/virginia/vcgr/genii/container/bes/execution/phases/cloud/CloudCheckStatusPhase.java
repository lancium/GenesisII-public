package edu.virginia.vcgr.genii.container.bes.execution.phases.cloud;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.factory.ActivityStateEnumeration;

import com.jcraft.jsch.JSchException;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.cloud.CloudManager;
import edu.virginia.vcgr.genii.cloud.CloudMonitor;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionException;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionPhase;
import edu.virginia.vcgr.genii.container.bes.execution.TerminateableExecutionPhase;

public class CloudCheckStatusPhase implements ExecutionPhase,
	Serializable, TerminateableExecutionPhase{

	static final long serialVersionUID = 0L;

	private String _activityID;
	private String _besid;
	private String _workingDir;
	private String _completeFileName;
	private String _checkPhase;
	private Boolean _terminate=false;
	private int _tries = 0;
	private boolean _failed = false;
	private int _backoff = 20;

	static private Log _logger = LogFactory.getLog(CloudCheckStatusPhase.class);


	public CloudCheckStatusPhase(String activityID, String besid,
			String workingDir, String completeFileName, String checkPhase){
		_activityID = activityID;
		_besid = besid;
		_workingDir = workingDir;
		_completeFileName = completeFileName;
		_checkPhase = checkPhase;

	}

	@Override
	public ActivityState getPhaseState() {
		return new ActivityState(ActivityStateEnumeration.Running,
				"Polling Status of " + _checkPhase + " Phase", false);
	}

	@Override
	public void execute(ExecutionContext context) throws Throwable {
		CloudManager tManage = CloudMonitor.getManager(_besid);
		String resourceID  = tManage.aquireResource(_activityID);

		while(true){
			
			if (_tries > 9)
				throw new Exception(); //force to fail create custom exception in future

			if (_terminate){
				_logger.info("CloudBES: Activity " + _activityID + " " +
						_checkPhase + " phase terminated early");
				return;
			}
			
			try {
				//Exponential Backoff
				if (_failed){
					long sleep = (long) ((_backoff * 1000) *
							Math.exp(.5 * _tries));
					_logger.info("Failed to Execute " + _activityID + 
							" sleeping for " + sleep/1000 + " seconds");
					Thread.sleep(sleep);
					_failed = false;
				}

				_tries++;
				//Poll until complete
				while(!tManage.checkFile(resourceID, _workingDir + _completeFileName)){
					_logger.info("CloudBES: Activity " + _activityID + " waiting on " +
							_checkPhase + " phase");
					Thread.sleep(20000);

				}	
				break;

			} catch (JSchException e){
				_logger.error(e);
				_failed = true;
			}
		}



		_logger.info("CloudBES: Activity " + _activityID + " " + _checkPhase + " phase complete");

	}

	@Override
	public void terminate(boolean countAsFailedAttempt) throws ExecutionException {

		CloudManager tManage = CloudMonitor.getManager(_besid);

		if (tManage != null){
			try {

				_logger.info("CloudBES: Terminating " + _checkPhase + " Phase");
				String resourceID  = tManage.aquireResource(_activityID);	
				//Kill Job processes, (modify once no longer running as root to killall -9 -1
				tManage.sendCommand(resourceID,
						"killall -9 -g runScript.sh", System.out, System.err);
				tManage.sendCommand(resourceID,
						"killall -9 -g grid", System.out, System.err);

				//Wipe working directory
				tManage.sendCommand(resourceID, "rm -rf " + _workingDir +
						"/*", System.out, System.err);

				//Release resource
				tManage.releaseResource(_activityID);

				_terminate = true;

			} catch (Exception e) {
				_logger.error(e);
			}
		}
	}

}
