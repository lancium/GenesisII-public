package edu.virginia.vcgr.secrun;

import java.util.Properties;

public interface SecureRunnable
{
	/**
	 * This is the method that a secure runnable must implement in order to be
	 * run.
	 * 
	 * @param runProperties Any run properties that the client/container
	 * passes to the runnable.
	 * @return True if the secure runnable should be deleted for future runs,
	 * false otherwise (i.e. to keep it and run it again).
	 * @throws Throwable If anything goes wrong.  This automatically results in
	 * the secure runnable NOT being deleted and for the container/client to
	 * exit.
	 */
	public boolean run(Properties runProperties) throws Throwable;
}