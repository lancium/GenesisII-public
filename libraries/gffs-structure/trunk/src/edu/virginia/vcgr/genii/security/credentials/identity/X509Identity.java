package edu.virginia.vcgr.genii.security.credentials.identity;

import java.io.ObjectStreamException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * compatibility class that can read from old db blobs.
 */
public class X509Identity extends
		edu.virginia.vcgr.genii.security.credentials.X509Identity {
	static final long serialVersionUID = 0L;

	private static Log _logger = LogFactory.getLog(X509Identity.class);

	public X509Identity() {
		super();
	}

	private Object readResolve() throws ObjectStreamException {
		if (_logger.isTraceEnabled())
			_logger.trace("old x509 load: type is " + _type + " for "
					+ _identity[0].getSubjectDN());
		edu.virginia.vcgr.genii.security.credentials.X509Identity toReturn = new edu.virginia.vcgr.genii.security.credentials.X509Identity(
				_identity, _type);
		toReturn.setMask(_mask);
		return toReturn;
	}

	private Object writeReplace() throws ObjectStreamException {
		return readResolve();
	}
}
