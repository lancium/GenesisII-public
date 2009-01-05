package edu.virginia.vcgr.genii.container.rfork;

import java.io.IOException;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.container.rns.InternalEntry;

public interface RNSResourceFork extends ResourceFork
{
	public Iterable<InternalEntry> list(EndpointReferenceType exemplarEPR,
		String entryName) throws IOException;
	public EndpointReferenceType add(EndpointReferenceType exemplarEPR,
		String entryName, EndpointReferenceType entry) throws IOException;
	public boolean remove(String entryName)
		throws IOException;
	public EndpointReferenceType createFile(EndpointReferenceType exemplarEPR,
		String newFileName) throws IOException;
	public EndpointReferenceType mkdir(EndpointReferenceType exemplarEPR,
		String newDirectoryName) throws IOException;
}