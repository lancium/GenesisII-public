package edu.virginia.vcgr.genii.container.bes;

import edu.virginia.vcgr.genii.container.bes.execution.ExecutionException;

public interface BESPolicyListener
{
	public void suspend() throws ExecutionException;
	public void suspendOrKill() throws ExecutionException;
	public void resume() throws ExecutionException;
	
	public void kill() throws ExecutionException;
}