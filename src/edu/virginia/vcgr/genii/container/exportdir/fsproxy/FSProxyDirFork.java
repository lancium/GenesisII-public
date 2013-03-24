package edu.virginia.vcgr.genii.container.exportdir.fsproxy;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.g3.fsview.FSViewDirectoryEntry;
import edu.virginia.g3.fsview.FSViewEntry;
import edu.virginia.g3.fsview.FSViewEntryType;
import edu.virginia.g3.fsview.FSViewSession;
import edu.virginia.vcgr.genii.client.exportdir.FSProxyConstructionParameters;
import edu.virginia.vcgr.genii.container.resource.IResource;
import edu.virginia.vcgr.genii.container.resource.ResourceManager;
import edu.virginia.vcgr.genii.container.rfork.AbstractRNSResourceFork;
import edu.virginia.vcgr.genii.container.rfork.RNSResourceFork;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkInformation;
import edu.virginia.vcgr.genii.container.rfork.ResourceForkService;
import edu.virginia.vcgr.genii.container.rns.InternalEntry;
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.rwx.RWXMapping;

public class FSProxyDirFork extends AbstractRNSResourceFork implements RNSResourceFork
{
	private FSViewSession session() throws IOException
	{
		IResource resource = ResourceManager.getCurrentResource().dereference();
		FSProxyConstructionParameters consParms = (FSProxyConstructionParameters) resource
			.constructionParameters(FSProxyServiceImpl.class);

		return consParms.connectionInformation().openSession();
	}

	public FSProxyDirFork(ResourceForkService service, String forkPath)
	{
		super(service, forkPath);
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	final public EndpointReferenceType add(EndpointReferenceType exemplarEPR, String entryName, EndpointReferenceType entry)
		throws IOException
	{
		throw new IOException("Not allowed to add arbitrary endpoints to a " + "light-weight export.");
	}

	@Override
	@RWXMapping(RWXCategory.READ)
	public Iterable<InternalEntry> list(EndpointReferenceType exemplarEPR, String entryName) throws IOException
	{
		FSViewSession session = null;
		Collection<InternalEntry> ret = new LinkedList<InternalEntry>();

		try {
			session = session();
			FSViewEntry entry = session.lookup(getForkPath());
			if (entry.entryType() != FSViewEntryType.Directory) {
				// If we are listed as a directory, but we aren't, then its
				// because we are a file mounted as the root

				if (entryName == null || entryName.equals("root")) {
					ret.add(createInternalEntry(exemplarEPR, "root",
						new FSProxyFileFork(getService(), formForkPath("")).describe()));
				}

				return ret;
			}

			for (FSViewEntry child : ((FSViewDirectoryEntry) entry).listEntries()) {
				String dName = child.entryName();

				if (entryName == null || entryName.equals(dName)) {
					ResourceForkInformation info;

					if (child.entryType() == FSViewEntryType.Directory)
						info = new FSProxyDirFork(getService(), formForkPath(dName)).describe();
					else
						info = new FSProxyFileFork(getService(), formForkPath(dName)).describe();

					ret.add(createInternalEntry(exemplarEPR, dName, info));
				}
			}

			return ret;
		} finally {
			StreamUtils.close(session);
		}
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public boolean remove(String entryName) throws IOException
	{
		FSViewSession session = null;

		try {
			session = session();
			FSViewEntry entry = session.lookup(getForkPath());
			if (entry.entryType() != FSViewEntryType.Directory)
				throw new IOException(String.format("FSViewEntry %s is not a directory!", entry));
			((FSViewDirectoryEntry) entry).delete(entryName);
			return true;
		} finally {
			StreamUtils.close(session);
		}
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public EndpointReferenceType createFile(EndpointReferenceType exemplarEPR, String newFileName) throws IOException
	{
		FSViewSession session = null;

		try {
			session = session();
			FSViewEntry entry = session.lookup(getForkPath());
			if (entry.entryType() != FSViewEntryType.Directory)
				throw new IOException(String.format("FSViewEntry %s is not a directory!", entry));
			((FSViewDirectoryEntry) entry).createFile(newFileName);
			String forkPath = formForkPath(newFileName);
			ResourceForkService service = getService();

			return service.createForkEPR(forkPath, new FSProxyFileFork(service, forkPath).describe());
		} finally {
			StreamUtils.close(session);
		}
	}

	@Override
	@RWXMapping(RWXCategory.WRITE)
	public EndpointReferenceType mkdir(EndpointReferenceType exemplarEPR, String newDirectoryName) throws IOException
	{
		FSViewSession session = null;

		try {
			session = session();
			FSViewEntry entry = session.lookup(getForkPath());
			if (entry.entryType() != FSViewEntryType.Directory)
				throw new IOException(String.format("FSViewEntry %s is not a directory!", entry));
			((FSViewDirectoryEntry) entry).createDirectory(newDirectoryName);
			String forkPath = formForkPath(newDirectoryName);
			ResourceForkService service = getService();

			return service.createForkEPR(forkPath, new FSProxyDirFork(service, forkPath).describe());
		} finally {
			StreamUtils.close(session);
		}
	}
}