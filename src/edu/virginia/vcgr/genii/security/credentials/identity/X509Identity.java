package edu.virginia.vcgr.genii.security.credentials.identity;

import java.io.ObjectStreamException;

/**
 * compatibility class that can read from old db blobs.
 */
public class X509Identity extends edu.virginia.vcgr.genii.security.credentials.X509Identity
{
	static final long serialVersionUID = 0L;

	public X509Identity()
	{
		super();
	}

	private Object readResolve() throws ObjectStreamException
	{
		edu.virginia.vcgr.genii.security.credentials.X509Identity toReturn = new edu.virginia.vcgr.genii.security.credentials.X509Identity(
			_identity, _type);
		toReturn.setMask(_mask);
		return toReturn;
	}

	private Object writeReplace() throws ObjectStreamException
	{
		return readResolve();
	}
}
