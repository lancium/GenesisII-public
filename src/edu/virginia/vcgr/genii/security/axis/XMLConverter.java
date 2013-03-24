package edu.virginia.vcgr.genii.security.axis;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.security.XMLCompatible;
import edu.virginia.vcgr.genii.security.credentials.NuCredential;
import edu.virginia.vcgr.genii.security.credentials.TrustCredential;
import edu.virginia.vcgr.genii.security.credentials.X509Identity;

/**
 * Upgrades credentials that do not ordinarily produce Axis XML objects into ones that do. This
 * works on X509Identity, TrustCredential and UsernamePasswordIdentity.
 * 
 * @author ckoeritz
 */
public class XMLConverter
{
	private static Log _logger = LogFactory.getLog(XMLConverter.class);

	/**
	 * turns a simple NuCredential into an object that can produce Axis MessageElements, if there is
	 * any possible conversion. otherwise null is returned.
	 */
	public static XMLCompatible upscaleCredential(NuCredential toUpscale)
	{
		if (toUpscale instanceof XMLCompatible)
			return (XMLCompatible) toUpscale;
		if (toUpscale instanceof X509Identity)
			return new AxisX509Identity((X509Identity) toUpscale);
		if (toUpscale instanceof TrustCredential)
			return new AxisTrustCredential((TrustCredential) toUpscale);
		// uh-oh.
		_logger.error("failed to upscale credential to XMLCompatible type: " + toUpscale.toString());
		return null;
	}
}
