package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.bes.factory.ActivityStateEnumeration;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.fuse.GeniiFuse;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;
import fuse.FuseException;

public class TeardownFUSEPhase extends AbstractFUSEPhases
{
	static private Log _logger = LogFactory.getLog(TeardownFUSEPhase.class);

	static final long serialVersionUID = 0L;

	public TeardownFUSEPhase(String mountPoint)
	{
		super(mountPoint, new ActivityState(ActivityStateEnumeration.Running, "fuse-teardown", false));
	}

	@Override
	public void execute(ExecutionContext context) throws Throwable
	{
		long sleepTime = 250L;

		try {
			Thread.sleep(250L);
		} catch (InterruptedException ie) {
		}

		for (int lcv = 0; lcv < 5; lcv++) {
			try {
				File f = getMountPoint(context);
				GeniiFuse.unmountGenesisII(f, true);
				Thread.sleep(250L);
				if (!f.delete())
					throw new IOException("Unable to delete old fuse directory.");
				return;
			} catch (Throwable exception) {
				_logger.warn("Exception thrown while trying to unmount FUSE.", exception);
				Thread.sleep(sleepTime);
				sleepTime <<= 2;
			}
		}

		throw new FuseException("Unable to unmount FUSE file system.");
	}
}