package edu.virginia.vcgr.genii.container.bes.activity.forks;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.SQLException;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.container.bes.activity.BESActivity;
import edu.virginia.vcgr.genii.container.bes.activity.resource.IBESActivityResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.rfork.AbstractStreamableByteIOFactoryResourceFork;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkService;
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.rwx.RWXMapping;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ActivitySummaryResourceFork extends AbstractStreamableByteIOFactoryResourceFork
{
	static private Log _logger = LogFactory.getLog(ActivitySummaryResourceFork.class);

	public ActivitySummaryResourceFork(ResourceForkService service, String forkPath)
	{
		super(service, forkPath);
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public void modifyState(InputStream source) throws IOException
	{
		throw new IOException("Not allowed to modify the the queue summary.");
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public void snapshotState(OutputStream sink) throws IOException
	{
		ResourceKey rKey = getService().getResourceKey();
		IBESActivityResource resource = (IBESActivityResource) rKey.dereference();
		PrintStream ps = new PrintStream(sink);

		BESActivity activity = resource.findActivity();
		ActivityState state = activity.getState();

		ps.format("State:  %s\n", state);
		if (state.isFailedState()) {
			ps.print("\nFaults:");
			int lcv = 0;
			try {
				for (Throwable t : activity.getFaults()) {
					ps.format("\nFault #%d\n", lcv++);
					t.printStackTrace(ps);
					ps.println();
				}
			} catch (SQLException sqe) {
				ps.format("Unable to open data base to get list of faults.");
				_logger.info("exception occurred in snapshotState", sqe);
			}
		}

		ps.flush();
	}
}
