package edu.virginia.vcgr.genii.container.bes.activity.forks;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.sql.SQLException;

import edu.virginia.vcgr.genii.client.bes.ActivityState;
import edu.virginia.vcgr.genii.client.jsdl.personality.common.BESWorkingDirectory;
import edu.virginia.vcgr.genii.container.bes.activity.BESActivity;
import edu.virginia.vcgr.genii.container.bes.activity.resource.IBESActivityResource;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;
import edu.virginia.vcgr.genii.container.rfork.AbstractStreamableByteIOFactoryResourceFork;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkService;
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.rwx.RWXMapping;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ActivityJWDResourceFork extends AbstractStreamableByteIOFactoryResourceFork
{
	static private Log _logger = LogFactory.getLog(ActivityJWDResourceFork.class);

	public ActivityJWDResourceFork(ResourceForkService service, String forkPath)
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
		BESWorkingDirectory workingDir = activity.getActivityCWD();
		File ret = workingDir.getWorkingDirectory();
		String dirname=ret.getName();
		//ActivityState state = activity.getState();
		ps.println(dirname);
			ps.flush();
	}
	
}
