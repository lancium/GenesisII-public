package edu.virginia.vcgr.genii.container.rfork;

public abstract class AbstractRandomByteIOResourceFork extends AbstractByteIOResourceFork implements RandomByteIOResourceFork
{
	protected AbstractRandomByteIOResourceFork(ResourceForkService service, String forkPath)
	{
		super(service, forkPath);
	}
}