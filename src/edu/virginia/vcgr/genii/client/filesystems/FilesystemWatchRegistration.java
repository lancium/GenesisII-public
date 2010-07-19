package edu.virginia.vcgr.genii.client.filesystems;

public interface FilesystemWatchRegistration
{
	public void cancel();
	public void resetCallCount();
}