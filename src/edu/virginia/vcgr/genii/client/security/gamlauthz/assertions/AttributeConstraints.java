package edu.virginia.vcgr.genii.client.security.gamlauthz.assertions;

import java.io.Externalizable;
import java.util.Date;

public interface AttributeConstraints extends Externalizable  {
	/**
	 * Checks that the attribute is time-valid with respect to the supplied 
	 * date and any delegation depth requirements are met by the supplied
	 * delegationDepth.
	 */
	public void checkValidity(int delegationDepth, Date date) throws AttributeInvalidException;
	
}
