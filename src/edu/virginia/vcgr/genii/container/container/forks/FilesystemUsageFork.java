package edu.virginia.vcgr.genii.container.container.forks;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.filesystems.Filesystem;
import edu.virginia.vcgr.genii.client.filesystems.FilesystemManager;
import edu.virginia.vcgr.genii.client.filesystems.FilesystemUsageInformation;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXMapping;
import edu.virginia.vcgr.genii.client.utils.units.Size;
import edu.virginia.vcgr.genii.container.rfork.AbstractStreamableByteIOFactoryResourceFork;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkService;
import edu.virginia.vcgr.genii.security.RWXCategory;

public class FilesystemUsageFork
	extends AbstractStreamableByteIOFactoryResourceFork
{
	public FilesystemUsageFork(ResourceForkService service, String forkPath)
	{
		super(service, forkPath);
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public void modifyState(InputStream source) throws IOException
	{
		throw new IOException("Not allowed to modify the the filesystem summary.");
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public void snapshotState(OutputStream sink) throws IOException
	{
		FilesystemManager mgr = 
			ConfigurationManager.getCurrentConfiguration().filesystemManager();
		
		PrintStream ps = new PrintStream(sink);
		
		ps.print("Filesystem Summary\n\n");
		
		for (String fsName : mgr.filesystems())
		{
			Filesystem fs = mgr.lookup(fsName);
			FilesystemUsageInformation usageInfo = fs.currentUsage();
			
			ps.format("Filesystem \"%s\" at %s:  %s (%.2f%%) space free.\n", 
				fsName, fs.filesystemRoot(),
				new Size(usageInfo.spaceUsable()),
				usageInfo.percentAvailable());
		}
		
		ps.close();
	}
}