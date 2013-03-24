package edu.virginia.vcgr.genii.client.dialog;

public interface RunnableMenuItem
{
	public void run(DialogProvider provider) throws DialogException, UserCancelException;
}