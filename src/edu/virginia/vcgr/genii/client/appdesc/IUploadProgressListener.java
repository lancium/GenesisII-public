package edu.virginia.vcgr.genii.client.appdesc;

import java.io.File;

public interface IUploadProgressListener
{
	public void startingUpload(File localFile);
	public void finishedUpload(File localFile);
}