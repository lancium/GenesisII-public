package edu.virginia.vcgr.genii.client.comm;

import java.util.Collection;
import java.util.LinkedList;

import edu.virginia.vcgr.genii.security.credentials.NuCredential;

public class SecurityUpdateResults
{
	private Collection<NuCredential> _removedCredentials = new LinkedList<NuCredential>();
	private Collection<NuCredential> _renewedCredentials = new LinkedList<NuCredential>();

	public void noteRemovedCredential(NuCredential cred)
	{
		_removedCredentials.add(cred);
	}

	public void noteRenewedCredential(NuCredential cred)
	{
		_renewedCredentials.add(cred);
	}

	public Collection<NuCredential> removedCredentials()
	{
		return new LinkedList<NuCredential>(_removedCredentials);
	}

	public Collection<NuCredential> renewedCredentials()
	{
		return new LinkedList<NuCredential>(_renewedCredentials);
	}
}