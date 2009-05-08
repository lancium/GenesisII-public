package edu.virginia.vcgr.genii.client.security.credentials.identity;

import java.io.Externalizable;

import edu.virginia.vcgr.genii.client.security.Describable;
import edu.virginia.vcgr.genii.client.security.authz.acl.AclEntry;

/**
 * A user-principal identity.  Can be used in access control lists
 * for direct comparison.
 * 
 * @author dgm4d
 *
 */
public interface Identity extends Externalizable, Describable, AclEntry
{

}
