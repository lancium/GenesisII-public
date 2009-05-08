package edu.virginia.vcgr.genii.client.security.credentials;

import java.util.*;
import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.context.ICallingContext;

/**
 * Class for holding and managing a set of "outgoing" credentials
 * within the current calling context.
 * 
 * @author dmerrill
 * 
 */
public class TransientCredentials implements Serializable
{

	static public Log _logger = 
			LogFactory.getLog("edu.virginia.vcgr.genii.client.authn");

	static final long serialVersionUID = 0L;

	protected static final String TRANSIENT_CRED_PROP_NAME =
			"genii.client.security.authz.credentials";

	public ArrayList<GIICredential> _credentials =
			new ArrayList<GIICredential>();

	/**
	 * Retrieves the credentials from the calling context. Guaranteed to not be
	 * null (may be empty, however)
	 * 
	 * @param callingContext
	 * @return
	 */
	public static synchronized TransientCredentials getTransientCredentials(
			ICallingContext callingContext)
	{
		TransientCredentials retval =
				(TransientCredentials) callingContext
						.getTransientProperty(TransientCredentials.TRANSIENT_CRED_PROP_NAME);
		if (retval == null)
		{
			retval = new TransientCredentials();
			callingContext.setTransientProperty(
					TransientCredentials.TRANSIENT_CRED_PROP_NAME, retval);
		}
		return retval;
	}

	public static synchronized void globalLogout(ICallingContext callingContext)
	{
		callingContext
				.removeTransientProperty(TransientCredentials.TRANSIENT_CRED_PROP_NAME);
		
		_logger.debug("Clearing current calling context credentials.");
	}

}
