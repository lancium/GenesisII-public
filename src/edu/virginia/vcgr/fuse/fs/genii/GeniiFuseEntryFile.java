package edu.virginia.vcgr.fuse.fs.genii;

import java.util.Collection;

import edu.virginia.vcgr.fuse.exceptions.FuseEntryNotDirectoryException;
import edu.virginia.vcgr.fuse.exceptions.FuseIOException;
import edu.virginia.vcgr.fuse.fs.FuseFileSystemEntry;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import fuse.FuseException;

abstract class GeniiFuseEntryFile extends GeniiFuseEntryCommon
{
	GeniiFuseEntryFile(RNSPath target, TypeInformation typeInfo,
			GeniiFuseFileSystemContext fsContext) throws FuseException
	{
		super(target, typeInfo, fsContext);
	}

	@Override
	public boolean isDirectory()
	{
		return false;
	}

	@Override
	public boolean isFile()
	{
		return true;
	}

	@Override
	public boolean isSymlink()
	{
		return false;
	}

	@Override
	public Collection<FuseFileSystemEntry> listContents() throws FuseException
	{
		throw new FuseEntryNotDirectoryException("Entry is not a directory.");
	}

	@Override
	public FuseFileSystemEntry readlink() throws FuseException
	{
		throw new FuseIOException(
			"Cannot read link information from a non-link.");
	}
}
