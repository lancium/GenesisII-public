package edu.virginia.vcgr.genii.ui.plugins.logs.tree;

import edu.virginia.vcgr.genii.ui.ApplicationContext;
import edu.virginia.vcgr.genii.ui.EndpointType;

public class LogFilledInTreeObject extends DefaultLogTreeObject
{
	private LogPath _path;
	private boolean _isLocal;

	public LogFilledInTreeObject(LogPath path) throws Throwable
	{
		super(LogTreeObjectType.ENDPOINT_OBJECT);
		if (path == null)
			throw new IllegalArgumentException("Path cannot be null.");
		_path = path;
		_isLocal = EndpointType.isLocal(_path.getEndpoint());
	}

	final public boolean isLocal()
	{
		return _isLocal;
	}

	final public LogPath path()
	{
		return _path;
	}

	@Override
	final public boolean allowsChildren()
	{
		return true;
	}

	@Override
	final public String toString()
	{
		return _path.getName();
	}

	final boolean isLocal(ApplicationContext appContext)
	{
		return appContext.isLocal(_path.getTargetEndpoint());
	}
}