package edu.virginia.vcgr.genii.client.security.gamlauthz.assertions;

import java.io.ObjectStreamException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;

public class RenewableClientAssertion extends DelegatedAssertion implements RenewableAssertion {
	
	static public final long serialVersionUID = 0L;

	private transient PrivateKey _privateKey;	
	
	// zero-arg contstructor for externalizable use only!
	public RenewableClientAssertion() {}
	
	public RenewableClientAssertion(
			RenewableClientAttribute delegatedAttribute, 
			PrivateKey privateKey) throws GeneralSecurityException {
		
		super(delegatedAttribute, privateKey);
		_privateKey = privateKey;
	}	
	
	/**
	 * Rewew this assertion
	 */
	public void renew() throws GeneralSecurityException {
		// renew the attribute
		((RenewableAttribute) _delegatedAttribute).rewew();

		// re-sign the attribute
		_delegatorSignature = SignedAttributeAssertion.sign(_delegatedAttribute, _privateKey);
	}
	
	public Object writeReplace() throws ObjectStreamException {
		DelegatedAssertion retval = new DelegatedAssertion ();
		retval._delegatedAttribute = _delegatedAttribute;
		retval._delegatorSignature = _delegatorSignature;

		return retval;
	}	

}
