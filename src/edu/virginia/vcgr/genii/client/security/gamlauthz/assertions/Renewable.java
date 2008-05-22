package edu.virginia.vcgr.genii.client.security.gamlauthz.assertions;

import java.security.GeneralSecurityException;

/**
 * Interface indicating that this type of object can be renewed
 * 
 * @author dgm4d
 */
public interface Renewable
{

	/**
	 * Renew this object
	 */
	public void renew() throws GeneralSecurityException;
}
