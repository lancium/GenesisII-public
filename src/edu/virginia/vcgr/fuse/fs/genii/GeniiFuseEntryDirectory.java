package edu.virginia.vcgr.fuse.fs.genii;

import java.util.Calendar;
import java.util.Collection;
import java.util.LinkedList;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.fuse.exceptions.FuseEntryIsDirectoryException;
import edu.virginia.vcgr.fuse.exceptions.FuseExceptions;
import edu.virginia.vcgr.fuse.fs.FuseFile;
import edu.virginia.vcgr.fuse.fs.FuseFileSystemEntry;
import edu.virginia.vcgr.genii.client.common.GenesisIIBaseRP;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyManager;
import fuse.FuseException;

class GeniiFuseEntryDirectory extends GeniiFuseEntryCommon
{
	GeniiFuseEntryDirectory(RNSPath target, TypeInformation typeInfo, 
		GeniiFuseFileSystemContext fsContext) throws FuseException
	{
		super(target, typeInfo, fsContext);
	}
	
	@Override
	protected GenesisIIBaseRP createResourcePropertiesHandler(
		EndpointReferenceType target, GeniiFuseFileSystemContext fsContext) 
		throws ResourcePropertyException
	{
		return (GenesisIIBaseRP)ResourcePropertyManager.createRPInterface(
			target, GenesisIIBaseRP.class);
	}

	@Override
	public Calendar accessTime() throws FuseException
	{
		return Calendar.getInstance();
	}

	@Override
	public void accessTime(Calendar time) throws FuseException
	{
		// do nothing
	}

	@Override
	public Calendar createTime() throws FuseException
	{
		return _fsContext.getMountTime();
	}

	@Override
	public boolean isDirectory()
	{
		return true;
	}

	@Override
	public boolean isFile()
	{
		return false;
	}

	@Override
	public boolean isSymlink()
	{
		return false;
	}

	@Override
	public long length() throws FuseException
	{
		return 0;
	}

	@Override
	public Collection<FuseFileSystemEntry> listContents() throws FuseException
	{
		Collection<FuseFileSystemEntry> ret = new LinkedList<FuseFileSystemEntry>();
		
		try
		{
			for (RNSPath entry : _targetPath.listContents())
				ret.add(new GeniiFuseEntryFacade(entry, _fsContext));
			
			return ret;
		}
		catch (Throwable cause)
		{
			throw FuseExceptions.translate("Unable to list contents.", cause);
		}
	}

	@Override
	public Calendar modificationTime() throws FuseException
	{
		return Calendar.getInstance();
	}

	@Override
	public void modificationTime(Calendar time) throws FuseException
	{
		// do nothing
	}

	@Override
	public FuseFile open(boolean create, boolean exclusiveCreate,
			boolean readable, boolean writable, boolean append,
			Long truncateLength) throws FuseException
	{
		throw new FuseEntryIsDirectoryException("Entry is a directory.");
	}

	@Override
	public FuseFileSystemEntry readlink() throws FuseException
	{
		throw new FuseEntryIsDirectoryException("Entry is a directory.");
	}
}