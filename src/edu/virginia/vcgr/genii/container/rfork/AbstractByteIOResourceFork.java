package edu.virginia.vcgr.genii.container.rfork;

public abstract class AbstractByteIOResourceFork extends AbstractResourceFork implements ByteIOResourceFork
{
	protected AbstractByteIOResourceFork(ResourceForkService service, String forkPath)
	{
		super(service, forkPath);
	}
}