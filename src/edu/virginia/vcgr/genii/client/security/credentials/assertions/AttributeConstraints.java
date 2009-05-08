package edu.virginia.vcgr.genii.client.security.credentials.assertions;

import java.io.Externalizable;
import java.util.Date;

import edu.virginia.vcgr.genii.client.security.Describable;

/**
 * Interface for attribute-based constraint/restriction policy
 * 
 * @author dgm4d
 *
 */
public interface AttributeConstraints extends Externalizable, Describable
{
	/**
	 * Checks that the attribute is time-valid with respect to the supplied date
	 * and any delegation depth requirements are met by the supplied
	 * delegationDepth.
	 */
	public void checkValidity(int delegationDepth, Date date)
			throws AttributeInvalidException;

	public Date getExpiration();
}
