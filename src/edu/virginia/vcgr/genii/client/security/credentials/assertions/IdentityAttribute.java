package edu.virginia.vcgr.genii.client.security.credentials.assertions;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.security.cert.X509Certificate;

import edu.virginia.vcgr.genii.client.security.VerbosityLevel;
import edu.virginia.vcgr.genii.client.security.credentials.identity.*;


/**
 * Simple base class for GII "identity" attributes.
 */
public class IdentityAttribute extends BasicAttribute
{

	static public final long serialVersionUID = 0L;

	protected HolderOfKeyIdentity _identity;

	// zero-arg contstructor for externalizable use only!
	public IdentityAttribute()
	{
	}

	public IdentityAttribute(HolderOfKeyIdentity identity)
	{
		super(null);
		_identity = identity;
	}

	public IdentityAttribute(AttributeConstraints constraints,
			HolderOfKeyIdentity identity)
	{
		super(constraints);
		_identity = identity;
	}

	public X509Certificate[] getAssertingIdentityCertChain()
	{
		return _identity.getAssertingIdentityCertChain();
	}

	public Identity getIdentity()
	{
		return _identity;
	}

	public String toString()
	{
		return describe(VerbosityLevel.HIGH);
	}
	
	@Override
	public String describe(VerbosityLevel verbosity)
	{
		if (verbosity.compareTo(VerbosityLevel.HIGH) >= 0)
		{
			return String.format("IdentityAttribute (%s %s)",
				((_constraints == null) ? "" : _constraints.toString()),
				((_identity == null) ? "" : _identity.describe(verbosity)));
		} else
		{
			if (_constraints != null && 
				(verbosity.compareTo(VerbosityLevel.LOW) >= 0))
				return String.format("%s %s",
					_constraints.describe(verbosity), 
					_identity.describe(verbosity));
			else
				return _identity.describe(verbosity);
		}
	}

	public void writeExternal(ObjectOutput out) throws IOException
	{
		super.writeExternal(out);
		out.writeObject(_identity);
	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException
	{
		super.readExternal(in);
		_identity = (HolderOfKeyIdentity) in.readObject();
	}

}
