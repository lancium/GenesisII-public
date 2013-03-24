package edu.virginia.vcgr.genii.container.bes.execution.phases.cloud;

import java.io.OutputStream;

import org.apache.commons.logging.Log;

import com.jcraft.jsch.JSchException;

import edu.virginia.vcgr.genii.cloud.CloudManager;
import edu.virginia.vcgr.genii.cloud.CloudMonitor;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionException;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionPhase;
import edu.virginia.vcgr.genii.container.bes.execution.TerminateableExecutionPhase;

abstract class AbstractCloudExecutionPhase implements ExecutionPhase, TerminateableExecutionPhase
{

	private int _tries = 0;
	private int _backoff = 20;
	protected String _activityID;
	protected String _besid;
	protected String _workingDir;
	protected boolean _terminate = false;
	private boolean _failed = false;

	abstract protected Log getLog();

	abstract protected String getPhase();

	protected final void tryExecuteCommand(String resourceID, String command, OutputStream out, OutputStream err,
		CloudManager tManage) throws Exception
	{

		while (true) {
			if (_tries > 9)
				throw new Exception(); // force to fail create custom exception in future

			if (_terminate)
				break;

			try {
				// Exponential Backoff
				if (_failed) {
					long sleep = (long) ((_backoff * 1000) * Math.exp(.5 * _tries));
					getLog().info("Failed to Execute " + _activityID + " sleeping for " + sleep / 1000 + " seconds");
					Thread.sleep(sleep);
					_failed = false;
				}

				_tries++;
				tManage.sendCommand(resourceID, command, out, err);

				break;

			} catch (JSchException e) {
				getLog().error(e);
				_failed = true;
			}
		}

	}

	protected final void trySendFile(String resourceID, String localPath, String remotePath, CloudManager tManage)
		throws Exception
	{
		while (true) {

			if (_tries > 9)
				throw new Exception(); // force to fail create custom exception in future

			if (_terminate)
				break;

			try {
				// Exponential Backoff
				if (_failed) {
					long sleep = (long) ((_backoff * 1000) * Math.exp(.5 * _tries));
					getLog().info("Failed to Execute " + _activityID + " sleeping for " + sleep / 1000 + " seconds");
					Thread.sleep(sleep);
					_failed = false;
				}

				_tries++;
				tManage.sendFileTo(resourceID, localPath, remotePath);
				break;

			} catch (JSchException e) {
				getLog().error(e);
				_failed = true;
			}
		}

	}

	protected final void tryRecieveFile(String resourceID, String localPath, String remotePath, CloudManager tManage)
		throws Exception
	{
		while (true) {

			if (_tries > 9)
				throw new Exception(); // force to fail create custom exception in future

			if (_terminate)
				break;

			try {
				// Exponential Backoff
				if (_failed) {
					long sleep = (long) ((_backoff * 1000) * Math.exp(.5 * _tries));
					getLog().info("Failed to Execute " + _activityID + " sleeping for " + sleep / 1000 + " seconds");
					Thread.sleep(sleep);
					_failed = false;
				}

				_tries++;
				tManage.recieveFileFrom(resourceID, localPath, remotePath);
				break;

			} catch (JSchException e) {
				getLog().error(e);
				_failed = true;
			}
		}

	}

	@Override
	public void terminate(boolean countAsFailedAttempt) throws ExecutionException
	{

		CloudManager tManage = CloudMonitor.getManager(_besid);
		// reset attempt counter
		_tries = 0;

		if (tManage != null) {
			try {

				getLog().info("CloudBES: Terminating " + getPhase() + " Phase");
				String resourceID = tManage.aquireResource(_activityID);
				// Kill Job processes, (modify once no longer running as root to killall -9 -1
				tryExecuteCommand(resourceID, "killall -9 -g runScript.sh", System.out, System.err, tManage);
				tryExecuteCommand(resourceID, "killall -9 -g grid", System.out, System.err, tManage);

				// Wipe working directory
				tryExecuteCommand(resourceID, "rm -rf " + _workingDir + "/*", System.out, System.err, tManage);

				// Release resource
				tManage.releaseResource(_activityID);

				_terminate = true;

			} catch (Exception e) {
				getLog().error(e);
			}
		}
	}

}
