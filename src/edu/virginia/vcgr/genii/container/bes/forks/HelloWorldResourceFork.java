package edu.virginia.vcgr.genii.container.bes.forks;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXMapping;
import edu.virginia.vcgr.genii.container.rfork.AbstractStreamableByteIOFactoryResourceFork;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkService;
import edu.virginia.vcgr.genii.security.RWXCategory;

public class HelloWorldResourceFork
	extends AbstractStreamableByteIOFactoryResourceFork
{
	public HelloWorldResourceFork(ResourceForkService service,
		String forkPath)
	{
		super(service, forkPath);
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public void modifyState(InputStream source) throws IOException
	{
		throw new IOException("Not allowed to modify the the hello world summary.");
	}

	@Override
	@RWXMapping(RWXCategory.OPEN)
	public void snapshotState(OutputStream sink) throws IOException
	{
		PrintStream ps = new PrintStream(sink);
		ps.println("Hello, World!");
		ps.flush();
	}
}