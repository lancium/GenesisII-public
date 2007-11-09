package edu.virginia.vcgr.genii.client.security.gamlauthz;

import java.util.Date;

import edu.virginia.vcgr.genii.client.security.gamlauthz.assertions.AttributeInvalidException;

/**
 * A credential object
 * 
 * @author dmerrill
 *
 */
public interface GamlCredential {

	/**
	 * Checks that the attribute time-valid with respect to the supplied 
	 * date
	 */
	public void checkValidity(Date date) throws AttributeInvalidException;

}
