package edu.virginia.vcgr.genii.security.identity;

import java.io.Externalizable;

import edu.virginia.vcgr.genii.security.Describable;
import edu.virginia.vcgr.genii.security.acl.AclEntry;

/**
 * A user-principal identity. Can be used in access control lists for direct comparison.
 * 
 * @author dmerrill
 */
public interface Identity extends Externalizable, Describable, AclEntry
{
	public boolean placeInUMask();

	public IdentityType getType();

	public void setType(IdentityType type);
}
