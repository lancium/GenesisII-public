package edu.virginia.vcgr.fuse.fs.genii;

import java.util.Calendar;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.fuse.exceptions.FuseExceptions;
import edu.virginia.vcgr.fuse.fs.FuseFile;
import edu.virginia.vcgr.genii.client.common.GenesisIIBaseRP;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyManager;
import fuse.FuseException;

class GeniiFuseEntryGenericEndpoint extends GeniiFuseEntryFile
{
	private byte []_content;
	
	GeniiFuseEntryGenericEndpoint(RNSPath target, TypeInformation typeInfo, 
		GeniiFuseFileSystemContext fsContext) throws FuseException
	{
		super(target, typeInfo, fsContext);
		
		try
		{
			_content = EPRUtils.toBytes(_target);
		}
		catch (Throwable t)
		{
			throw FuseExceptions.translate("Unable to serialize EPR.", t);
		}
	}
	
	@Override
	protected GenesisIIBaseRP createResourcePropertiesHandler(
			EndpointReferenceType target, GeniiFuseFileSystemContext fsContext)
			throws ResourcePropertyException
	{
		return (GenesisIIBaseRP)ResourcePropertyManager.createRPInterface(
			_target, GenesisIIBaseRP.class);
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
	public long length() throws FuseException
	{
		return _content.length;
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
		// TODO Auto-generated method stub
		return null;
	}
}