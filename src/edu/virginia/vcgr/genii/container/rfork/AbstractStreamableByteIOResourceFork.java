package edu.virginia.vcgr.genii.container.rfork;

import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXMapping;
import edu.virginia.vcgr.genii.container.resource.ResourceKey;

public abstract class AbstractStreamableByteIOResourceFork 
	extends AbstractByteIOResourceFork 
	implements StreamableByteIOResourceFork
{
	static final private String POSITION_PROPERTY_FORMAT_STRING =
		"edu.virginia.vcgr.genii.rfork.sbyteio.position.%s";
	static final private String DIRTY_PROPERTY_FORMAT_STRING =
		"edu.virginia.vcgr.genii.rfork.sbyteio.dirty.%s";
	
	protected AbstractStreamableByteIOResourceFork(
		ResourceForkService service, String forkPath)
	{
		super(service, forkPath);
	}

	protected void setPosition(long newPosition)
	{
		try
		{
			ResourceKey rKey = getService().getResourceKey();
			rKey.dereference().setProperty(String.format(
				POSITION_PROPERTY_FORMAT_STRING, getForkPath()),
				new Long(newPosition));
		}
		catch (Exception re)
		{
			throw new RuntimeException("Unable to get resource property.", re);
		}
	}
	
	protected void setDirty()
	{
		try
		{
			ResourceKey rKey = getService().getResourceKey();
			rKey.dereference().setProperty(String.format(
				DIRTY_PROPERTY_FORMAT_STRING, getForkPath()),
				new Boolean(true));
		}
		catch (Exception re)
		{
			throw new RuntimeException("Unable to get resource property.", re);
		}
	}
	
	@Override
	public long getPosition()
	{
		try
		{
			ResourceKey rKey = getService().getResourceKey();
			Long position = (Long)rKey.dereference().getProperty(String.format(
				POSITION_PROPERTY_FORMAT_STRING, getForkPath()));
			if (position == null)
				return 0L;
			return position.longValue();
		}
		catch (Exception re)
		{
			throw new RuntimeException("Unable to get resource property.", re);
		}
	}
	
	@Override
	@RWXMapping(RWXCategory.OPEN)
	public void destroy() throws ResourceException
	{
		super.destroy();
	}
	
	protected boolean isDirty()
	{
		boolean ret = false;
		
		try
		{
			ResourceKey rKey = getService().getResourceKey();
			Boolean dirty = (Boolean)rKey.dereference().getProperty(String.format(
				DIRTY_PROPERTY_FORMAT_STRING, getForkPath()));
			if (dirty == null)
				ret = false;
			else
				ret = dirty.booleanValue();
		}
		catch (Exception re)
		{
			throw new RuntimeException("Unable to get resource property.", re);
		}
		
		return ret;
	}
}