package edu.virginia.vcgr.genii.client.security.gamlauthz.assertions;

import java.io.ObjectStreamException;

import edu.virginia.vcgr.genii.client.security.gamlauthz.identity.AssertableIdentity;

public class RenewableIdentityAttribute extends IdentityAttribute 
		implements RenewableAttribute {
	
	static public final long serialVersionUID = 0L;

	// zero-arg contstructor for externalizable use only!
	public RenewableIdentityAttribute() {}
	
	public RenewableIdentityAttribute(
			long notValidBeforeMillis, 
			long durationValidMillis, 
			long maxDelegationDepth,
			AssertableIdentity identity) {

		super(
				notValidBeforeMillis, 
				durationValidMillis, 
				maxDelegationDepth, 
				identity);
	}	

	/**
	 * Rewew this assertion
	 */
	public void rewew() {
		_notValidBeforeMillis = 
			System.currentTimeMillis() - (1000L * 60 * 15); // 15 minutes ago
	}

	public Object writeReplace() throws ObjectStreamException {
		return new IdentityAttribute(
				_notValidBeforeMillis, 
				_durationValidMillis, 
				_maxDelegationDepth, 
				_identity);
	}	
}
