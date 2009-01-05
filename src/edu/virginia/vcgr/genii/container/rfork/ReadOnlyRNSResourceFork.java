package edu.virginia.vcgr.genii.container.rfork;

import java.io.IOException;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.security.authz.RWXCategory;
import edu.virginia.vcgr.genii.client.security.authz.RWXMapping;

public abstract class ReadOnlyRNSResourceFork extends AbstractRNSResourceFork
{
	protected ReadOnlyRNSResourceFork(ResourceForkService service,
			String forkPath)
	{
		super(service, forkPath);
	}
	
	@Override
	@RWXMapping(RWXCategory.OPEN)
	final public EndpointReferenceType add(EndpointReferenceType exemplarEPR,
			String entryName, EndpointReferenceType entry) throws IOException
	{
		throw new IOException("This RNS directory is read only!");
	}

	@Override
	@RWXMapping(RWXCategory.OPEN)
	final public EndpointReferenceType createFile(
		EndpointReferenceType exemplarEPR, String newFileName) 
			throws IOException
	{
		throw new IOException("This RNS directory is read only!");
	}

	@Override
	@RWXMapping(RWXCategory.OPEN)
	final public EndpointReferenceType mkdir(EndpointReferenceType exemplarEPR,
		String newDirectoryName) throws IOException
	{
		throw new IOException("This RNS directory is read only!");
	}

	@Override
	@RWXMapping(RWXCategory.OPEN)
	final public boolean remove(String entryName) throws IOException
	{
		throw new IOException("This RNS directory is read only!");
	}
}