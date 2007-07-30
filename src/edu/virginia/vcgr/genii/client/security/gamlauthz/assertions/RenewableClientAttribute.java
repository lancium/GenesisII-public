package edu.virginia.vcgr.genii.client.security.gamlauthz.assertions;

import java.io.ObjectStreamException;
import java.security.GeneralSecurityException;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.security.x509.KeyAndCertMaterial;

public class RenewableClientAttribute extends DelegatedAttribute implements RenewableAttribute {

	static public final long serialVersionUID = 0L;

	protected transient ICallingContext _callingContext;
	
	// zero-arg contstructor for externalizable use only!
	public RenewableClientAttribute() {}
	
	public RenewableClientAttribute(
			RenewableAttributeAssertion assertion,
			ICallingContext callingContext) throws GeneralSecurityException {	
		
		_callingContext = callingContext;

		// grab the delegatee identity from the calling context
		KeyAndCertMaterial clientKeyMaterial = ClientUtils.getActiveKeyAndCertMaterial(
				_callingContext);
		
		if ((assertion == null) || (clientKeyMaterial._clientCertChain == null)) {
			throw new java.lang.IllegalArgumentException(
					"DelegatedAttribute constructor cannot accept null parameters");
		}
		
		_assertion = assertion;
		_delegateeIdentity = clientKeyMaterial._clientCertChain;
	}
	
	/**
	 * Rewew this attribute
	 */
	public void rewew() throws GeneralSecurityException {
		// rewew the wrapped assertion
		((RenewableAttributeAssertion) _assertion).renew();	
		
		// grab the delegatee identity from the calling context
		KeyAndCertMaterial clientKeyMaterial = ClientUtils.getActiveKeyAndCertMaterial(
				_callingContext);
		_delegateeIdentity = clientKeyMaterial._clientCertChain;
	}
	
	public Object writeReplace() throws ObjectStreamException {
		return new DelegatedAttribute (
				_assertion,
				_delegateeIdentity);
	}	
	
}
