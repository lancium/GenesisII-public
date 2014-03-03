package edu.virginia.vcgr.externalapp;

import java.io.File;

public interface ExternalApplicationCallback
{
	public void externalApplicationFailed(Throwable cause);

	/**
	 * This callback method is used if the application exits successfully
	 * 
	 * @param contentFile
	 *            The contentFile that was changed (if no changes were made or saved, this parameter
	 *            is null).
	 */
	public void externalApplicationExited(File contentFile);
}