package edu.virginia.vcgr.genii.client.security.authz.acl;

import java.io.Serializable;
import java.security.GeneralSecurityException;

import edu.virginia.vcgr.genii.client.security.Describable;
import edu.virginia.vcgr.genii.client.security.credentials.identity.*;

/**
 * An element in an ACL policy collection indicating a specific 
 * authorization rule
 * 
 * @author dgm4d
 */
public interface AclEntry extends Serializable, Describable {

	/**
	 * Returns false if policy does not yield access to the 
	 * given Identity.  Throws an exception if there is an issue with 
	 * the validity of the identity validity checking.
	 * 
	 * @param identity
	 * @throws GeneralSecurityException
	 */
	public boolean isPermitted(Identity identity) throws GeneralSecurityException;
	
}
