package edu.virginia.vcgr.genii.client.pwrapper;

public interface ProcessWrapperToken
{
	public void cancel();

	/* Returns null if the process hasn't exited. */
	public ExitResults results() throws ProcessWrapperException;

	public void join() throws InterruptedException;
}