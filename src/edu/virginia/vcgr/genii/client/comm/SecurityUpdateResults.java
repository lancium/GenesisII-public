package edu.virginia.vcgr.genii.client.comm;

import java.util.Collection;
import java.util.LinkedList;

import edu.virginia.vcgr.genii.client.security.gamlauthz.GamlCredential;

public class SecurityUpdateResults
{
	private Collection<GamlCredential> _removedCredentials = 
		new LinkedList<GamlCredential>();
	private Collection<GamlCredential> _renewedCredentials =
		new LinkedList<GamlCredential>();
	
	public void noteRemovedCredential(GamlCredential cred)
	{
		_removedCredentials.add(cred);
	}
	
	public void noteRenewedCredential(GamlCredential cred)
	{
		_renewedCredentials.add(cred);
	}
	
	public Collection<GamlCredential> removedCredentials()
	{
		return new LinkedList<GamlCredential>(_removedCredentials);
	}
	
	public Collection<GamlCredential> renewedCredentials()
	{
		return new LinkedList<GamlCredential>(_renewedCredentials);
	}
}