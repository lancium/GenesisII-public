package edu.virginia.vcgr.genii.container.rfork;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXMapping;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMultiplexer;

public abstract class AbstractResourceFork implements ResourceFork
{
	private ResourceForkService _service;
	private String _forkPath;
	private String _forkName = null;
	
	protected AbstractResourceFork(
		ResourceForkService service, String forkPath)
	{
		_service = service;
		_forkPath = forkPath.replaceAll("/{2,}", "/");
	}
	
	protected ResourceForkService getService()
	{
		return _service;
	}
	
	protected String getForkName()
	{
		if (_forkName == null)
		{
			int index = _forkPath.lastIndexOf('/');
			if (index < 0)
				throw new RuntimeException(
					"Invalid fork path given \"" + _forkPath + 
					"\"-- must contain a slash character.");
			_forkName = _forkPath.substring(index + 1);
		}
		
		return _forkName;
	}
	
	@Override
	public void registerNotificationHandlers(NotificationMultiplexer multiplexer)
	{
		// By default, there is nothing to do.
	}

	@Override
	public String getForkPath()
	{
		return _forkPath;
	}
	
	@Override
	@RWXMapping(RWXCategory.WRITE)
	public void destroy() throws ResourceException
	{
		// Do nothing
	}
	
	@Override
	public ResourceForkInformation describe()
	{
		return new DefaultResourceForkInformation(
			getClass(), getForkPath());
	}
}