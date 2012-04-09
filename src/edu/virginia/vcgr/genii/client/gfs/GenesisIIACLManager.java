package edu.virginia.vcgr.genii.client.gfs;

import java.security.GeneralSecurityException;
import java.util.ArrayList;
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
import edu.virginia.vcgr.genii.security.credentials.identity.IdentityType;

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
			if (isAllowed && !acls.contains(null))
				acls.add(null);
			else if (!isAllowed && acls.contains(null))
				acls.remove(null);
		} else
		{
			for (Identity id : callerIds)
			{
				if (isAllowed && !acls.contains(id))
					acls.add(id);
				else if (!isAllowed && acls.contains(id))
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
		if (acl != null)
		{
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
		}
		return p;
	}
	
	public void setPermissions(Permissions p) throws AuthZSecurityException
	{
		Acl acl = getRemoteACL();

		//Separate Group and user credentials

		Collection<Identity> _userIdentities = new ArrayList<Identity>();
		Collection<Identity> _groupIdentities = new ArrayList<Identity>();

		for (Identity id : _callerIdentities){
			if (id.getType().equals(IdentityType.USER))
				_userIdentities.add(id);
			else if (id.getType().equals(IdentityType.GROUP))
				_groupIdentities.add(id);

			//currently do not add any other type of identity
		}


		setPermission(acl.readAcl, p.isSet(PermissionBits.OWNER_READ), 
				_userIdentities);
		setPermission(acl.writeAcl, p.isSet(PermissionBits.OWNER_WRITE), 
				_userIdentities);
		setPermission(acl.executeAcl, p.isSet(PermissionBits.OWNER_EXECUTE), 
				_userIdentities);


		setPermission(acl.readAcl, p.isSet(PermissionBits.GROUP_READ), 
				_groupIdentities);
		setPermission(acl.writeAcl, p.isSet(PermissionBits.GROUP_WRITE), 
				_groupIdentities);
		setPermission(acl.executeAcl, p.isSet(PermissionBits.GROUP_EXECUTE), 
				_groupIdentities);


		setPermission(acl.readAcl, p.isSet(PermissionBits.EVERYONE_READ), 
				null);
		setPermission(acl.writeAcl, p.isSet(PermissionBits.EVERYONE_WRITE), 
				null);
		setPermission(acl.executeAcl, p.isSet(PermissionBits.EVERYONE_EXECUTE), 
				null);

		AuthZConfig config = Acl.encodeAcl(acl);
		_rpStub.setAuthZConfig(config);
	}

	
	//This method gets the original acl after a resource is created (i.e 
	//determines container side default acls), and then reduces the set based
	//on the passed in Permissions (but does not add to it)
	//also ignores group and other bits only relies on owner
	public void setCreatePermissions(Permissions p) throws AuthZSecurityException
	{
		Acl acl = getRemoteACL();
		setCreatePermission(acl.readAcl, p.isSet(PermissionBits.OWNER_READ), 
				_callerIdentities);
		setCreatePermission(acl.writeAcl, p.isSet(PermissionBits.OWNER_WRITE), 
				_callerIdentities);
		setCreatePermission(acl.executeAcl, p.isSet(PermissionBits.OWNER_EXECUTE), 
				_callerIdentities);
		
		AuthZConfig config = Acl.encodeAcl(acl);
		_rpStub.setAuthZConfig(config);
	}
	
	private void setCreatePermission(Collection<AclEntry> acls,
			boolean isAllowed, Collection<Identity> callerIds)
		{
			if (callerIds == null)
			{
				//Do not add wild card acl on create resource
			} else
			{
				for (Identity id : callerIds)
				{
					if (!isAllowed && acls.contains(id))
						acls.remove(id);
					
					//Do not add any acls that are not 
					//in the default acl generated by container side cod
					//if (isAllowed)
				}	//	acls.add(id);
			}
		}
}