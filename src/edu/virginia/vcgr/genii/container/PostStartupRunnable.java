package edu.virginia.vcgr.genii.container;

public class PostStartupRunnable implements Runnable
{
	private IContainerManaged _service;
	
	public PostStartupRunnable(IContainerManaged service)
	{
		_service = service;
	}
	
	@Override
	public void run()
	{
		_service.postStartup();
	}
}