package edu.virginia.vcgr.genii.container;

public interface IContainerManaged
{
	public void cleanupHook();

	/**
	 * Called by the container to initialize the service. Returns true if the service is being
	 * *created* for the first time.
	 */
	public boolean startup();

	public void postStartup();
}