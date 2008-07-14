package edu.virginia.vcgr.fuse.fs.genii;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.fuse.exceptions.FuseExceptions;
import edu.virginia.vcgr.fuse.exceptions.FuseUnknownException;
import edu.virginia.vcgr.fuse.fs.FuseFileSystemEntry;
import edu.virginia.vcgr.fuse.server.PermissionCategory;
import edu.virginia.vcgr.fuse.server.PermissionIdentity;
import edu.virginia.vcgr.genii.client.common.GenesisIIBaseRP;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.naming.WSName;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import fuse.FuseException;
import fuse.FuseStat;

abstract class GeniiFuseEntryCommon implements FuseFileSystemEntry
{
	static private Log _logger = LogFactory.getLog(GeniiFuseEntryCommon.class);
	
	protected RNSPath _targetPath;
	protected EndpointReferenceType _target;
	protected GeniiFuseFileSystemContext _fsContext;
	protected GenesisIIBaseRP _resourceProperties;
	protected TypeInformation _typeInfo;
	protected GeniiFusePermissions _permissions;
	
	static public long generateInodeNumber(EndpointReferenceType target)
	{
		WSName name = new WSName(target);
		if (name.isValidWSName())
		{
			return name.getEndpointIdentifier().toString().hashCode();
		} else
		{
			_logger.warn("Trying to generate an INode number of a target which"
				+ "does not implement the WS-Naming specification.");
			
			try
			{
				byte []array = EPRUtils.toBytes(target);
				long result = 0;
				for (byte d : array)
				{
					result ^= d;
				}
				
				return result;
			}
			catch (ResourceException re)
			{
				_logger.fatal("Unexpected error while trying to serialize EPR.", re);
				throw new RuntimeException(re);
			}
		}
	}
	
	protected abstract GenesisIIBaseRP createResourcePropertiesHandler(
		EndpointReferenceType target, GeniiFuseFileSystemContext fsContext)
			throws ResourcePropertyException;
	
	GeniiFuseEntryCommon(RNSPath target, 
		TypeInformation typeInfo, GeniiFuseFileSystemContext fsContext)
			throws FuseException
	{
		try
		{
			_targetPath = target;
			_target = target.getEndpoint();
			_fsContext = fsContext;
			_typeInfo = typeInfo;
			_permissions = null;
			
			_resourceProperties = createResourcePropertiesHandler(
				_target, fsContext);
		}
		catch (Throwable cause)
		{
			throw FuseExceptions.translate("Unable to setup entry.", cause);
		}
	}
	
	@Override
	public void delete(boolean onlyUnlink) throws FuseException
	{
		// This shouldn't really get called
		throw new FuseUnknownException(
			"The delete operation shouldn't really get called.");
	}

	@Override
	public boolean exists()
	{
		return true;
	}

	@Override
	public void flush() throws FuseException
	{
	}

	@Override
	public int getPermissions() throws FuseException
	{
		if (_permissions == null)
			_permissions = new GeniiFusePermissions(
				_fsContext.getCallerIdentities(), _resourceProperties);
		
		int mode = 0x0;
		
		mode |= (_permissions.hasAccess(PermissionIdentity.USER, 
			PermissionCategory.READ)) ? FuseStat.OWNER_READ : 0x0;
		mode |= (_permissions.hasAccess(PermissionIdentity.USER, 
			PermissionCategory.WRITE)) ? FuseStat.OWNER_WRITE : 0x0;
		mode |= (_permissions.hasAccess(PermissionIdentity.USER, 
			PermissionCategory.EXECUTE)) ? FuseStat.OWNER_EXECUTE : 0x0;
		mode |= (_permissions.hasAccess(PermissionIdentity.OTHER, 
			PermissionCategory.READ)) ? FuseStat.OTHER_READ : 0x0;
		mode |= (_permissions.hasAccess(PermissionIdentity.OTHER, 
			PermissionCategory.WRITE)) ? FuseStat.OTHER_WRITE : 0x0;
		mode |= (_permissions.hasAccess(PermissionIdentity.OTHER, 
			PermissionCategory.EXECUTE)) ? FuseStat.OTHER_EXECUTE : 0x0;
		
		return mode;
	}

	@Override
	public long inode() throws FuseException
	{
		return generateInodeNumber(_target);
	}

	@Override
	public void link(FuseFileSystemEntry source) throws FuseException
	{
		// This shouldn't really get called
		throw new FuseUnknownException(
			"The link operation shouldn't really get called.");
	}

	@Override
	public void mkdir(int mode) throws FuseException
	{
		// This shouldn't really get called
		throw new FuseUnknownException(
			"The mkdir operation shouldn't really get called.");
	}

	@Override
	public String name()
	{
		// This shouldn't really get called.
		return null;
	}

	@Override
	public String pwd()
	{
		// This shouldn't really get called
		return null;
	}

	@Override
	public void rename(FuseFileSystemEntry source) throws FuseException
	{
		// This shouldn't really get called
		throw new FuseUnknownException(
			"The delete operation shouldn't really get called.");
	}

	@Override
	public void setPermissions(int mode) throws FuseException
	{
		if (_permissions == null)
			_permissions = new GeniiFusePermissions(
				_fsContext.getCallerIdentities(), _resourceProperties);
		
		_permissions.setPermission(PermissionIdentity.USER, 
			PermissionCategory.READ, (mode & FuseStat.OWNER_READ) > 0);
		_permissions.setPermission(PermissionIdentity.USER, 
			PermissionCategory.WRITE, (mode & FuseStat.OWNER_WRITE) > 0);
		_permissions.setPermission(PermissionIdentity.USER, 
			PermissionCategory.WRITE, (mode & FuseStat.OWNER_WRITE) > 0);
		_permissions.setPermission(PermissionIdentity.OTHER, 
			PermissionCategory.READ, (mode & FuseStat.OTHER_READ) > 0);
		_permissions.setPermission(PermissionIdentity.OTHER, 
			PermissionCategory.WRITE, (mode & FuseStat.OTHER_WRITE) > 0);
		_permissions.setPermission(PermissionIdentity.OTHER, 
			PermissionCategory.WRITE, (mode & FuseStat.OTHER_WRITE) > 0);
		
		_permissions.flush();
	}

	@Override
	public void symlink(FuseFileSystemEntry source) throws FuseException
	{
		// This shouldn't really get called
		throw new FuseUnknownException(
			"The delete operation shouldn't really get called.");
	}
}