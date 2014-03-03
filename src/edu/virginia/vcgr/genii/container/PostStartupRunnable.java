package edu.virginia.vcgr.genii.container;

import edu.virginia.vcgr.genii.algorithm.structures.queue.IServiceWithCleanupHook;

public class PostStartupRunnable implements Runnable
{
	private IServiceWithCleanupHook _service;

	public PostStartupRunnable(IServiceWithCleanupHook service)
	{
		_service = service;
	}

	@Override
	public void run()
	{
		_service.postStartup();
	}
}