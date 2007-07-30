package edu.virginia.vcgr.genii.client.security.gamlauthz.assertions;

import java.security.GeneralSecurityException;

/**
 * Interface indicating that this type of signed assertion can be renewed
 * @author dgm4d
 */
public interface RenewableAssertion {

	/**
	 * Rewew this assertion
	 */
	public void renew() throws GeneralSecurityException;
}
