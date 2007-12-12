package edu.virginia.vcgr.genii.container.q2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.security.gamlauthz.AuthZSecurityException;
import edu.virginia.vcgr.genii.client.security.gamlauthz.GamlCredential;
import edu.virginia.vcgr.genii.client.security.gamlauthz.TransientCredentials;
import edu.virginia.vcgr.genii.client.security.gamlauthz.assertions.IdentityAttribute;
import edu.virginia.vcgr.genii.client.security.gamlauthz.assertions.SignedAssertion;
import edu.virginia.vcgr.genii.client.security.gamlauthz.identity.Identity;

public class QueueSecurity
{
	static public Collection<Identity> getCallerIdentities() 
		throws AuthZSecurityException
	{
		try
		{
			Collection<Identity> ret = new ArrayList<Identity>();
			ICallingContext callingContext = ContextManager.getCurrentContext(false);
			
			if (callingContext == null)
				throw new AuthZSecurityException(
					"Error processing GAML credential: No calling context");
			
			TransientCredentials transientCredentials = 
				TransientCredentials.getTransientCredentials(callingContext);
			for (GamlCredential cred : transientCredentials._credentials) 
			{
				if (cred instanceof Identity) 
				{
					ret.add((Identity)cred);
				} else if (cred instanceof SignedAssertion) 
				{
					SignedAssertion signedAssertion = (SignedAssertion)cred;
					
					// if its an identity assertion, check it against our ACLs
					if (signedAssertion.getAttribute() instanceof IdentityAttribute) 
					{
						IdentityAttribute identityAttr = 
							(IdentityAttribute) signedAssertion.getAttribute();
	
						ret.add(identityAttr.getIdentity());
					}
				}
			}
			
			return ret;
		}
		catch (ConfigurationException ce)
		{
			throw new AuthZSecurityException("Unable to load current context.", ce);
		}
		catch (IOException ioe)
		{
			throw new AuthZSecurityException("Unable to load current context.", ioe);
		}
	}
	

	static public boolean isOwner(Collection<Identity> jobOwners) 
		throws AuthZSecurityException
	{
		Collection<Identity> callers = QueueSecurity.getCallerIdentities();
		
		if (jobOwners == null || jobOwners.size() == 0)
			return true;
		
		for (Identity jobOwner : jobOwners)
		{
			if (isOwner(callers, jobOwner))
				return true;
		}
		
		return false;
	}
	
	static public boolean isOwner(Collection<Identity> callers,
		Identity jobOwner) throws AuthZSecurityException
	{
		if (jobOwner == null)
			return true;
		
		if (callers.contains(jobOwner))
			return true;
		
		return false;
	}
}