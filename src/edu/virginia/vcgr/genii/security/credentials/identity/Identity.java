package edu.virginia.vcgr.genii.security.credentials.identity;

import java.io.Externalizable;

import edu.virginia.vcgr.genii.client.security.authz.acl.AclEntry;
import edu.virginia.vcgr.genii.security.Describable;

/**
 * A user-principal identity.  Can be used in access control lists
 * for direct comparison.
 * 
 * @author dgm4d
 *
 */
public interface Identity extends Externalizable, Describable, AclEntry
{
   public boolean placeInUMask();
   public IdentityType getType();
   public void setType(IdentityType type);
}
