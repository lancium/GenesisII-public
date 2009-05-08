package edu.virginia.vcgr.genii.client.security.credentials.assertions;

import java.io.ObjectStreamException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;

public class RenewableAttributeAssertion extends SignedAttributeAssertion
		implements Renewable
{

	static public final long serialVersionUID = 0L;

	private transient PrivateKey _privateKey;

	// zero-arg contstructor for externalizable use only!
	public RenewableAttributeAssertion()
	{
	}

	public RenewableAttributeAssertion(Attribute attribute,
			PrivateKey privateKey) throws GeneralSecurityException
	{

		super(attribute, privateKey);
		_privateKey = privateKey;
	}

	/**
	 * Renew this assertion
	 */
	public void renew() throws GeneralSecurityException
	{

		// renew the attribute
		if ((_attribute != null) && (_attribute instanceof Renewable))
		{
			((Renewable) _attribute).renew();
		}

		// re-sign the attribute
		_signature = sign(_attribute, _privateKey);
	}

	public Object writeReplace() throws ObjectStreamException
	{
		SignedAttributeAssertion retval = new SignedAttributeAssertion();
		retval._attribute = _attribute;
		retval._signature = _signature;

		return retval;
	}
}
