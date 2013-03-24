package edu.virginia.vcgr.genii.client.security.authz.acl;

import java.io.ObjectStreamException;

import javax.security.auth.x500.X500Principal;

import edu.virginia.vcgr.genii.security.credentials.X509Identity;

/**
 * compatibility class that can read from old db blobs.
 */
public class X509PatternAclEntry extends edu.virginia.vcgr.genii.security.acl.X509PatternAclEntry
{
	static final long serialVersionUID = 0L;

	protected X509Identity _trustRoot;
	protected X500Principal _userPattern;

	X509PatternAclEntry()
	{
		super(null, null);
	}

	private Object readResolve() throws ObjectStreamException
	{
		edu.virginia.vcgr.genii.security.acl.X509PatternAclEntry toReturn = new edu.virginia.vcgr.genii.security.acl.X509PatternAclEntry(
			this._trustRoot, this._userPattern);
		// toReturn.fixCachedMembers(this._trustManager, this._bcPattern);
		return toReturn;
	}

	private Object writeReplace() throws ObjectStreamException
	{
		return readResolve();
	}
}
