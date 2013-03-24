package edu.virginia.vcgr.genii.container.bes.execution.phases.cloud;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.factory.ActivityStateEnumeration;

import com.jcraft.jsch.JSchException;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.history.HistoryEventCategory;
import edu.virginia.vcgr.genii.cloud.CloudManager;
import edu.virginia.vcgr.genii.cloud.CloudMonitor;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;
import edu.virginia.vcgr.genii.container.bes.execution.TerminateableExecutionPhase;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContext;
import edu.virginia.vcgr.genii.container.cservices.history.HistoryContextFactory;

public class CloudCheckStatusPhase extends AbstractCloudExecutionPhase implements Serializable, TerminateableExecutionPhase
{

	static final long serialVersionUID = 0L;

	private String _activityID;
	private String _besid;
	private String _workingDir;
	private String _completeFileName;
	private String _checkPhase;
	private Boolean _terminate = false;
	private int _tries = 0;
	private boolean _failed = false;
	private int _backoff = 20;

	static private Log _logger = LogFactory.getLog(CloudCheckStatusPhase.class);

	public CloudCheckStatusPhase(String activityID, String besid, String workingDir, String completeFileName, String checkPhase)
	{
		_activityID = activityID;
		_besid = besid;
		_workingDir = workingDir;
		_completeFileName = completeFileName;
		_checkPhase = checkPhase;

	}

	@Override
	public ActivityState getPhaseState()
	{
		return new ActivityState(ActivityStateEnumeration.Running, "polling-status-" + _checkPhase, false);
	}

	@Override
	public void execute(ExecutionContext context) throws Throwable
	{
		CloudManager tManage = CloudMonitor.getManager(_besid);
		String resourceID = tManage.aquireResource(_activityID);

		HistoryContext history = HistoryContextFactory.createContext(HistoryEventCategory.Checking);

		history.createInfoWriter("Polling for completion of " + _checkPhase + " Phase").close();

		while (true) {

			if (_tries > 9)
				throw new Exception(); // force to fail create custom exception in future

			if (_terminate) {
				_logger.info("CloudBES: Activity " + _activityID + " " + _checkPhase + " phase terminated early");
				return;
			}

			try {
				// Exponential Backoff
				if (_failed) {
					long sleep = (long) ((_backoff * 1000) * Math.exp(.5 * _tries));
					_logger.info("Failed to Execute " + _activityID + " sleeping for " + sleep / 1000 + " seconds");
					Thread.sleep(sleep);
					_failed = false;
					_tries++;
				}

				// Poll
				if (tManage.checkFile(resourceID, _workingDir + _completeFileName)) {
					break;
				} else {
					_logger.info("CloudBES: Activity " + _activityID + " waiting on " + _checkPhase + " phase");
					Thread.sleep(20000);
				}

			} catch (JSchException e) {
				_logger.error(e);
				_failed = true;
			}
		}

		history.createInfoWriter("Completed " + _checkPhase + " Phase").close();
		_logger.info("CloudBES: Activity " + _activityID + " " + _checkPhase + " phase complete");

	}

	@Override
	protected Log getLog()
	{
		return _logger;
	}

	@Override
	protected String getPhase()
	{
		return "Polling " + _checkPhase;
	}

}
