package edu.virginia.vcgr.genii.client.filesystems;

public interface FilesystemWatchFilter
{
	public boolean matches(FilesystemUsageInformation usageInformation);
}