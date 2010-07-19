package edu.virginia.vcgr.genii.client.filesystems;

public interface FilesystemWatchHandler
{
	public void notifyFilesystemEvent(FilesystemManager manager,
		String filesystemName, Filesystem filesystem,
		FilesystemUsageInformation usageInformation,
		boolean matchedConstraints);
}