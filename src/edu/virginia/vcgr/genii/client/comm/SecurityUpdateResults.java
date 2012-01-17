package edu.virginia.vcgr.genii.client.comm;

import java.util.Collection;
import java.util.LinkedList;

import edu.virginia.vcgr.genii.security.credentials.GIICredential;

public class SecurityUpdateResults
{
	private Collection<GIICredential> _removedCredentials = 
		new LinkedList<GIICredential>();
	private Collection<GIICredential> _renewedCredentials =
		new LinkedList<GIICredential>();
	
	public void noteRemovedCredential(GIICredential cred)
	{
		_removedCredentials.add(cred);
	}
	
	public void noteRenewedCredential(GIICredential cred)
	{
		_renewedCredentials.add(cred);
	}
	
	public Collection<GIICredential> removedCredentials()
	{
		return new LinkedList<GIICredential>(_removedCredentials);
	}
	
	public Collection<GIICredential> renewedCredentials()
	{
		return new LinkedList<GIICredential>(_renewedCredentials);
	}
}