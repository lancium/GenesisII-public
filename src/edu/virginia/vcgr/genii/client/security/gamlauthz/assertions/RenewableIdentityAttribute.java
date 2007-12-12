package edu.virginia.vcgr.genii.client.security.gamlauthz.assertions;

import java.io.ObjectStreamException;

import edu.virginia.vcgr.genii.client.security.gamlauthz.identity.AssertableIdentity;
import java.security.GeneralSecurityException;

public class RenewableIdentityAttribute extends IdentityAttribute 
		implements Renewable {
	
	static public final long serialVersionUID = 0L;

	// zero-arg contstructor for externalizable use only!
	public RenewableIdentityAttribute() {}
	
	public RenewableIdentityAttribute(
			AttributeConstraints constraints, 
			AssertableIdentity identity) {

		super(
				constraints, 
				identity);
	}	

	/**
	 * Renew this assertion
	 */
	public void renew() throws GeneralSecurityException {
		
		// renew any constraints
		if ((_constraints != null) && (_constraints instanceof Renewable)) {
			((Renewable) _constraints).renew(); 
		}
		
		if ((_identity != null) && (_identity instanceof Renewable)) {
			((Renewable) _identity).renew(); 
		}
	}

	public Object writeReplace() throws ObjectStreamException {
		return new IdentityAttribute(
				_constraints, 
				_identity);
	}	
}
