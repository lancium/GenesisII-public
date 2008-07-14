package edu.virginia.vcgr.fuse.fs.genii;

import java.util.Calendar;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.fuse.exceptions.FuseExceptions;
import edu.virginia.vcgr.fuse.fs.FuseFile;
import edu.virginia.vcgr.genii.client.byteio.RandomByteIORP;
import edu.virginia.vcgr.genii.client.common.GenesisIIBaseRP;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyManager;
import fuse.FuseException;

class GeniiFuseEntryRByteIO extends GeniiFuseEntryFile
{
	GeniiFuseEntryRByteIO(RNSPath target, TypeInformation typeInfo,
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
			_target, GenesisIIBaseRP.class, RandomByteIORP.class);
	}

	@Override
	public Calendar accessTime() throws FuseException
	{
		return ((RandomByteIORP)_resourceProperties).getAccessTime();
	}

	@Override
	public void accessTime(Calendar time) throws FuseException
	{
		((RandomByteIORP)_resourceProperties).setAccessTime(time);
	}

	@Override
	public Calendar createTime() throws FuseException
	{
		return ((RandomByteIORP)_resourceProperties).getCreateTime();
	}

	@Override
	public long length() throws FuseException
	{
		return ((RandomByteIORP)_resourceProperties).getSize();
	}

	@Override
	public Calendar modificationTime() throws FuseException
	{
		return ((RandomByteIORP)_resourceProperties).getModificationTime();
	}

	@Override
	public void modificationTime(Calendar time) throws FuseException
	{
		((RandomByteIORP)_resourceProperties).setModificationTime(time);
	}

	@Override
	public FuseFile open(boolean create, boolean exclusiveCreate,
			boolean readable, boolean writable, boolean append,
			Long truncateLength) throws FuseException
	{
		try
		{
			return new RandomByteIOFuseFile(_target, _fsContext,
				readable, writable, append, truncateLength);
		}
		catch (Throwable t)
		{
			throw FuseExceptions.translate("Unable to open file.", t);
		}
	}
}