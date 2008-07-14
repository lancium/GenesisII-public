package edu.virginia.vcgr.fuse.fs.genii;

import java.util.Calendar;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.fuse.exceptions.FuseExceptions;
import edu.virginia.vcgr.fuse.fs.FuseFile;
import edu.virginia.vcgr.genii.client.byteio.StreamableByteIORP;
import edu.virginia.vcgr.genii.client.common.GenesisIIBaseRP;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyManager;
import fuse.FuseException;

class GeniiFuseEntrySByteIO extends GeniiFuseEntryFile
{
	GeniiFuseEntrySByteIO(RNSPath target, TypeInformation typeInfo, 
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
			_target, GenesisIIBaseRP.class, StreamableByteIORP.class);
	}

	@Override
	public Calendar accessTime() throws FuseException
	{
		return ((StreamableByteIORP)_resourceProperties).getAccessTime();
	}

	@Override
	public void accessTime(Calendar time) throws FuseException
	{
		((StreamableByteIORP)_resourceProperties).setAccessTime(time);
	}

	@Override
	public Calendar createTime() throws FuseException
	{
		return ((StreamableByteIORP)_resourceProperties).getCreateTime();
	}

	@Override
	public long length() throws FuseException
	{
		Long size = ((StreamableByteIORP)_resourceProperties).getSize();
		if (size == null)
			return Long.MAX_VALUE;
		
		return size;
	}

	@Override
	public Calendar modificationTime() throws FuseException
	{
		return ((StreamableByteIORP)_resourceProperties).getModificationTime();
	}

	@Override
	public void modificationTime(Calendar time) throws FuseException
	{
		((StreamableByteIORP)_resourceProperties).setModificationTime(time);
	}

	@Override
	public FuseFile open(boolean create, boolean exclusiveCreate,
			boolean readable, boolean writable, boolean append,
			Long truncateLength) throws FuseException
	{
		try
		{
			return new StreamableByteIOFuseFile(_target, _fsContext,
				readable, writable, append, truncateLength);
		}
		catch (Throwable t)
		{
			throw FuseExceptions.translate("Unable to open file.", t);
		}
	}
}