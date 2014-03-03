package edu.virginia.vcgr.genii.algorithm.structures.queue;

public interface IServiceWithCleanupHook
{
	public void cleanupHook();

	/**
	 * Called by to initialize the service. Returns true if the service is being *created* for the
	 * first time.
	 */
	public boolean startup();

	public void postStartup();
}
