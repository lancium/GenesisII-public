package edu.virginia.vcgr.genii.client.gfs;

import java.security.GeneralSecurityException;
import java.util.Collection;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.fsii.security.PermissionBits;
import edu.virginia.vcgr.fsii.security.Permissions;
import edu.virginia.vcgr.genii.client.common.GenesisIIBaseRP;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyManager;
import edu.virginia.vcgr.genii.client.security.authz.AuthZSecurityException;
import edu.virginia.vcgr.genii.client.security.authz.acl.Acl;
import edu.virginia.vcgr.genii.client.security.authz.acl.AclEntry;
import edu.virginia.vcgr.genii.common.security.AuthZConfig;
import edu.virginia.vcgr.genii.security.credentials.identity.Identity;

public class GenesisIIACLManager
{
	private Acl _remoteACL = null;
	
	private Collection<Identity> _callerIdentities;
	private GenesisIIBaseRP _rpStub;
	
	public GenesisIIACLManager(EndpointReferenceType target,
		Collection<Identity> callerIdentities) 
			throws ResourcePropertyException
	{
		_callerIdentities = callerIdentities;
		_rpStub = 
			(GenesisIIBaseRP)ResourcePropertyManager.createRPInterface(
				target, GenesisIIBaseRP.class);
	}
		
	private Acl getRemoteACL() throws AuthZSecurityException
	{
		if (_remoteACL == null)
		{
			AuthZConfig config = _rpStub.getAuthZConfig();
			_remoteACL = Acl.decodeAcl(config);
		}
		
		return _remoteACL;
	}
	
	static private boolean hasPermission(Collection<AclEntry> acls, 
		Collection<Identity> callerIds)
	{
		for (AclEntry entry : acls)
		{
			if (entry == null)
				return true;
			
			if (callerIds != null) {

				for (Identity id : callerIds) 
				{
					try {
						if (entry.isPermitted(id)) {
							return true;
						}
					} catch (GeneralSecurityException e) {
					}
				}

			}
		}
		
		return false;
	}
	
	private void setPermission(Collection<AclEntry> acls,
		boolean isAllowed, Collection<Identity> callerIds)
	{
		if (callerIds == null)
		{
			if (isAllowed)
				acls.add(null);
			else
				acls.remove(null);
		} else
		{
			for (Identity id : callerIds)
			{
				if (isAllowed)
					acls.add(id);
				else
					acls.remove(id);
			}
		}
	}
	
	public Permissions getPermissions() throws AuthZSecurityException
	{
		return _rpStub.getPermissions();
	}
	
	static public Permissions getPermissions(
		Acl acl, Collection<Identity> callerIdentities)
	{
		Permissions p = new Permissions();
		
		p.set(PermissionBits.OWNER_READ, hasPermission(
			acl.readAcl, callerIdentities));
		p.set(PermissionBits.OWNER_WRITE, hasPermission(
			acl.writeAcl, callerIdentities));
		p.set(PermissionBits.OWNER_EXECUTE, hasPermission(
			acl.executeAcl, callerIdentities));
		p.set(PermissionBits.EVERYONE_READ, hasPermission(
			acl.readAcl, null));
		p.set(PermissionBits.EVERYONE_WRITE, hasPermission(
			acl.writeAcl, null));
		p.set(PermissionBits.EVERYONE_EXECUTE, hasPermission(
			acl.executeAcl, null));
		
		return p;
	}
	
	public void setPermissions(Permissions p) throws AuthZSecurityException
	{
		Acl acl = getRemoteACL();
		setPermission(acl.readAcl, p.isSet(PermissionBits.OWNER_READ), 
			_callerIdentities);
		setPermission(acl.writeAcl, p.isSet(PermissionBits.OWNER_WRITE), 
			_callerIdentities);
		setPermission(acl.executeAcl, p.isSet(PermissionBits.OWNER_EXECUTE), 
			_callerIdentities);
		setPermission(acl.readAcl, p.isSet(PermissionBits.EVERYONE_READ), 
			null);
		setPermission(acl.writeAcl, p.isSet(PermissionBits.EVERYONE_WRITE), 
			null);
		setPermission(acl.executeAcl, p.isSet(PermissionBits.EVERYONE_EXECUTE), 
			null);
		
		AuthZConfig config = Acl.encodeAcl(acl);
		_rpStub.setAuthZConfig(config);
	}
}