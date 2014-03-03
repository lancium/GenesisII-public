package edu.virginia.vcgr.genii.security.credentials;

import java.io.Externalizable;
import java.security.cert.X509Certificate;
import java.util.Date;

import edu.virginia.vcgr.genii.security.Describable;
import edu.virginia.vcgr.genii.security.faults.AttributeInvalidException;

/**
 * A GenesisII credential object.
 * 
 * @author dmerrill
 * @author ckoeritz
 */
public interface NuCredential extends Externalizable, Describable {
	/**
	 * Checks that the attribute time-valid with respect to the supplied date
	 * and that any delegation depth limits are met by the credential.
	 */
	public void checkValidity(Date date) throws AttributeInvalidException;

	/**
	 * Returns the identity of the original credential asserter.
	 */
	public X509Certificate[] getOriginalAsserter();
}
