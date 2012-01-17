package edu.virginia.vcgr.genii.client.context;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import edu.virginia.vcgr.genii.security.credentials.GIICredential;

final public class CallingContextUtilities
{
	static public ICallingContext setupCallingContextAfterCombinedExtraction(
		ICallingContext context)
	{
		// Remove any serialized caller-credentials from the message header 
		Collection<Serializable> signedAssertions = 
			context.getProperty(GIICredential.ENCODED_GAML_CREDENTIALS_PROPERTY);
		context.removeProperty(GIICredential.ENCODED_GAML_CREDENTIALS_PROPERTY);

		// Deserialize the encoded caller-credentials and add them 
		// to a "caller-only" cred-set: they will be added to the transient 
		// cred-set later (along with any other creds conveyed outside the 
		// calling-context) during security-processing.
		ArrayList<GIICredential> callerCredentials =
			new ArrayList<GIICredential>();
		if (signedAssertions != null) 
		{
			Iterator<Serializable> itr = signedAssertions.iterator();
			while (itr.hasNext()) 
			{
				GIICredential signedAssertion = (GIICredential) itr.next();
				callerCredentials.add(signedAssertion);
			}
		}
		context.setTransientProperty(GIICredential.CALLER_CREDENTIALS_PROPERTY, 
			callerCredentials);
		
		return context;
	}
}