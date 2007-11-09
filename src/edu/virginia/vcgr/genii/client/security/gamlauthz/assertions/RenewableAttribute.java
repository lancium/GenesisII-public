package edu.virginia.vcgr.genii.client.security.gamlauthz.assertions;

import java.security.GeneralSecurityException;

public interface RenewableAttribute extends Attribute {

	/**
	 * Rewew this attribute
	 */
	public void renew() throws GeneralSecurityException;
}
