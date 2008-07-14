package edu.virginia.vcgr.fuse.fs.genii;

import java.util.Collection;

import edu.virginia.vcgr.fuse.exceptions.FusePermissionDeniedException;
import edu.virginia.vcgr.fuse.server.PermissionCategory;
import edu.virginia.vcgr.fuse.server.PermissionIdentity;
import edu.virginia.vcgr.genii.client.common.GenesisIIBaseRP;
import edu.virginia.vcgr.genii.client.security.gamlauthz.AuthZSecurityException;
import edu.virginia.vcgr.genii.client.security.gamlauthz.GamlAcl;
import edu.virginia.vcgr.genii.client.security.gamlauthz.identity.Identity;
import edu.virginia.vcgr.genii.common.security.AuthZConfig;
import fuse.FuseException;

public class GeniiFusePermissions
{
	private boolean _dirty = false;
	private GamlAcl _localACL = null;
	private GamlAcl _remoteACL = null;
	
	private Collection<Identity> _callerIdentities;
	private GenesisIIBaseRP _resourcePropertyHandler;
	
	public GeniiFusePermissions(
		Collection<Identity> callerIdentities,
		GenesisIIBaseRP resourcePropertyHandler)
	{
		_callerIdentities = callerIdentities;
		_resourcePropertyHandler = resourcePropertyHandler;
	}

	private Collection<Identity> getCategoriesACL(PermissionCategory category)
		throws FuseException
	{
		synchronized(this)
		{
			if (_localACL == null)
			{
				try
				{
					AuthZConfig config = _resourcePropertyHandler.getAuthZConfig();
					_localACL = GamlAcl.decodeAcl(config);
				}
				catch (AuthZSecurityException azse)
				{
					_localACL = new GamlAcl();
				}
				
				_remoteACL = (GamlAcl)_localACL.clone();
			}
		}
		
		Collection<Identity> identityCollection;
		if (category.equals(PermissionCategory.READ))
			identityCollection = _localACL.readAcl;
		else if (category.equals(PermissionCategory.WRITE))
			identityCollection = _localACL.writeAcl;
		else
			identityCollection = _localACL.executeAcl;
		
		return identityCollection;
	}
	
	public boolean hasAccess(
		PermissionIdentity identity, PermissionCategory category) 
			throws FuseException
	{
		Collection<Identity> identityCollection = getCategoriesACL(category);
		Collection<Identity> callerIdentities = _callerIdentities;
		for (Identity id : identityCollection)
		{
			if (id == null)
				return true;
						
			if (identity.equals(PermissionIdentity.USER) && callerIdentities.contains(id))
				return true;
		}
		
		return false;
	}
	
	public void setPermission(PermissionIdentity identity, 
		PermissionCategory category, boolean givePermission)
			throws FuseException
	{
		Collection<Identity> identityCollection = getCategoriesACL(category);
		if (identity.equals(PermissionIdentity.OTHER))
		{
			if (givePermission)
				identityCollection.add(null);
			else
				identityCollection.remove(null);
		} else
		{
			for (Identity me : _callerIdentities)
			{
				if (givePermission)
					identityCollection.add(me);
				else
					identityCollection.remove(me);
			}
		}
		
		_dirty = true;
	}
	
	public void flush() throws FuseException
	{
		try
		{
			if (_dirty)
			{
				AuthZConfig config = GamlAcl.encodeAcl(_localACL);
				_resourcePropertyHandler.setAuthZConfig(config);
				
				_dirty = false;
				_remoteACL = (GamlAcl)_localACL.clone();
			}
		}
		catch (AuthZSecurityException azse)
		{
			throw new FusePermissionDeniedException(
				"Unable to set new access control.", azse);
		}
		finally
		{
			if (_dirty)
			{
				_dirty = false;
				_localACL = (GamlAcl)_remoteACL.clone();
			}
		}
	}
}