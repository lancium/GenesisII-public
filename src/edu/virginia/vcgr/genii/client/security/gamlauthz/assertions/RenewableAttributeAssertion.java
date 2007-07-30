package edu.virginia.vcgr.genii.client.security.gamlauthz.assertions;

import java.io.ObjectStreamException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;

public class RenewableAttributeAssertion extends SignedAttributeAssertion 
		implements RenewableAssertion {

	static public final long serialVersionUID = 0L;
	
	private transient PrivateKey _privateKey;	
	
	// zero-arg contstructor for externalizable use only!
	public RenewableAttributeAssertion() {}
	
	public RenewableAttributeAssertion(
			RenewableAttribute attribute, 
			PrivateKey privateKey) throws GeneralSecurityException {

		super(attribute, privateKey);
		_privateKey = privateKey;
	}
	
	/**
	 * Rewew this assertion
	 */
	public void renew() throws GeneralSecurityException {
		// renew the attribute
		((RenewableAttribute) _attribute).rewew();

		// re-sign the attribute
		_signature = sign(_attribute, _privateKey);
	}
	
	public Object writeReplace() throws ObjectStreamException {
		SignedAttributeAssertion retval = new SignedAttributeAssertion ();
		retval._attribute = _attribute;
		retval._signature = _signature;

		return retval;
	}		
}
