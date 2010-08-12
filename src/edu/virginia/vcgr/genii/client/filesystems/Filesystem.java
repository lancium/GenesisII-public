package edu.virginia.vcgr.genii.client.filesystems;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public interface Filesystem
{
	void addSandbox(String sandboxName, String relativePath,
		boolean doCreate) throws FileNotFoundException;
	
	public Set<FilesystemProperties> properties();
	
	public File filesystemRoot();
	
	public FilesystemSandbox openSandbox(String sandboxName)
		throws FileNotFoundException;
	
	public FilesystemWatchRegistration addWatch(Integer callLimit, 
		long checkPeriod, TimeUnit checkPeriodUnits, 
		FilesystemWatchFilter filter, FilesystemWatchHandler handler);
}