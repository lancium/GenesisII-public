package edu.virginia.vcgr.genii.client.security.gamlauthz.assertions;

import java.io.ObjectStreamException;
import java.security.GeneralSecurityException;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.security.x509.KeyAndCertMaterial;
import java.security.cert.X509Certificate;

public class RenewableClientAttribute extends DelegatedAttribute 
		implements Renewable {

	static public final long serialVersionUID = 0L;

	protected transient ICallingContext _callingContext;
	
	// zero-arg contstructor for externalizable use only!
	public RenewableClientAttribute() {}
	
	public RenewableClientAttribute(
			AttributeConstraints constraints, 
			ICallingContext callingContext) 
		throws GeneralSecurityException {	
		
		_constraints = constraints;
		
		if (callingContext == null) {		
			throw new java.lang.IllegalArgumentException(
					"DelegatedAttribute constructor cannot accept null parameters");
		}

		_callingContext = callingContext;

		// grab the delegatee identity from the calling context (renewing it 
		// if necesssary -- dont want to sign to stale creds
		KeyAndCertMaterial clientKeyMaterial = ClientUtils.checkAndRenewCredentials(
				_callingContext);
		
		_delegateeIdentity = clientKeyMaterial._clientCertChain;
	}
	
	public RenewableClientAttribute(
			AttributeConstraints constraints,
			X509Certificate[] delegateeIdentity) 
		throws GeneralSecurityException {	

		_constraints = constraints;
		
		if ((delegateeIdentity == null) || (delegateeIdentity.length < 1) || (delegateeIdentity[0] == null)) {		
			throw new java.lang.IllegalArgumentException(
					"DelegatedAttribute constructor cannot accept null parameters");
		}
	
		_callingContext = null;
		_delegateeIdentity = delegateeIdentity;
	}
	
	public RenewableClientAttribute(
			AttributeConstraints constraints,
			RenewableAttributeAssertion assertion,
			ICallingContext callingContext) throws GeneralSecurityException {	
		
		_constraints = constraints;
		_callingContext = callingContext;

		// grab the delegatee identity from the calling context (renewing it 
		// if necesssary -- dont want to sign to stale creds
		KeyAndCertMaterial clientKeyMaterial = ClientUtils.checkAndRenewCredentials(
				_callingContext);
		
		if ((assertion == null) || (clientKeyMaterial._clientCertChain == null)) {
			throw new java.lang.IllegalArgumentException(
					"DelegatedAttribute constructor cannot accept null parameters");
		}
		
		_assertion = assertion;
		_delegateeIdentity = clientKeyMaterial._clientCertChain;
	}
	
	public void setAssertion(SignedAssertion assertion) throws GeneralSecurityException {
		if (assertion == null) {
			throw new java.lang.IllegalArgumentException(
					"DelegatedAttribute assertion cannot accept null parameters");
		}

		_assertion = assertion;
	}
	
	/**
	 * Renew this attribute
	 */
	public void renew() throws GeneralSecurityException {

		// renew the wrapped assertion
		if ((_assertion != null) && (_assertion instanceof Renewable)) {
			((Renewable) _assertion).renew(); 
		}
		
		// renew the constraints
		if ((_constraints != null) && (_constraints instanceof Renewable)) {
			((Renewable) _constraints).renew(); 
		}
		
		// replace the delegatee with the one that's currently in the calling context
		KeyAndCertMaterial clientKeyMaterial = _callingContext.getActiveKeyAndCertMaterial();
		_delegateeIdentity = clientKeyMaterial._clientCertChain;
	}
	
	public Object writeReplace() throws ObjectStreamException {
		return new DelegatedAttribute (
				_constraints,
				_assertion,
				_delegateeIdentity);
	}	
	
}
