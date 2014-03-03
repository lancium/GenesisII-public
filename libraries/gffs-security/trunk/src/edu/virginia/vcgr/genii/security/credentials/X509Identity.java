package edu.virginia.vcgr.genii.security.credentials;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.EnumSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.security.CertificateValidatorFactory;
import edu.virginia.vcgr.genii.security.RWXAccessible;
import edu.virginia.vcgr.genii.security.RWXCategory;
import edu.virginia.vcgr.genii.security.VerbosityLevel;
import edu.virginia.vcgr.genii.security.X500PrincipalUtilities;
import edu.virginia.vcgr.genii.security.acl.AclEntry;
import edu.virginia.vcgr.genii.security.faults.AttributeInvalidException;
import edu.virginia.vcgr.genii.security.identity.Identity;
import edu.virginia.vcgr.genii.security.identity.IdentityType;

/**
 * An Identity/Assertion wrapper for X.509 identities
 * 
 * @author dmerrill
 * @author ckoeritz
 */
public class X509Identity implements Identity, NuCredential, RWXAccessible {
	static public final long serialVersionUID = 0L;

	private static Log _logger = LogFactory.getLog(X509Identity.class);

	protected X509Certificate[] _identity;
	protected IdentityType _type = IdentityType.USER;
	protected EnumSet<RWXCategory> _mask = EnumSet.of(RWXCategory.READ,
			RWXCategory.WRITE, RWXCategory.EXECUTE);

	// zero-arg constructor for externalizable use only.
	public X509Identity() {
	}

	public X509Identity(X509Certificate[] identity) {
		_identity = identity;
	}

	public X509Identity(X509Certificate[] identity, IdentityType type) {
		this(identity);
		_type = type;
	}

	@Override
	public IdentityType getType() {
		return _type;
	}

	@Override
	public void setType(IdentityType type) {
		_type = type;
	}

	@Override
	public EnumSet<RWXCategory> getMask() {
		return _mask;
	}

	@Override
	public void setMask(EnumSet<RWXCategory> newMask) {
		_mask = newMask;
	}

	@Override
	public X509Certificate[] getOriginalAsserter() {
		// X509 certificates assert themselves via their own corresponding
		// private key
		return _identity;
	}

	public void setIdentity(X509Certificate[] newId) {
		_identity = newId;
	}

	/**
	 * Checks that the attribute is time-valid with respect to the supplied date
	 * and any delegation depth requirements are met by the supplied
	 * delegationDepth.
	 */
	@Override
	public void checkValidity(Date date) throws AttributeInvalidException {
		boolean okay = CertificateValidatorFactory.getValidator()
				.validateCertificateConsistency(_identity);
		if (!okay)
			throw new AttributeInvalidException(
					"failed to validate cert path: " + _identity.toString());

		try {
			for (X509Certificate cert : getOriginalAsserter()) {
				cert.checkValidity(date);
			}
		} catch (CertificateException e) {
			throw new AttributeInvalidException(
					"Security attribute asserting identity contains an invalid certificate: "
							+ e.getMessage(), e);
		}
	}

	@Override
	public String toString() {
		return describe(VerbosityLevel.HIGH);
	}

	@Override
	public String describe(VerbosityLevel verbosity) {
		switch (verbosity) {
		case HIGH:
			return String.format(
					"(" + _type.toString()
							+ ") \"%s\" [%2$tF %2$tT, %3$tF %3$tT]",
					X500PrincipalUtilities.describe(
							_identity[0].getSubjectX500Principal(), verbosity),
					_identity[0].getNotBefore(), _identity[0].getNotAfter());

		case MEDIUM:
			return String.format(
					"(" + _type.toString()
							+ ") \"%s\" [%2$tF %2$tT, %3$tF %3$tT]",
					X500PrincipalUtilities.describe(
							_identity[0].getSubjectX500Principal(), verbosity),
					_identity[0].getNotBefore(), _identity[0].getNotAfter());

		default:
			if (_type == IdentityType.USER) {
				// System.err.println("The identity type is user " +
				// _identity[0].getSubjectX500Principal().toString());
			}
			return ("(" + _type.toString() + ") " + X500PrincipalUtilities
					.describe(_identity[0].getSubjectX500Principal(), verbosity));
		}
	}

	@Override
	public int hashCode() {
		if ((_identity != null) && (_identity.length != 0))
			return _identity[0].hashCode();

		return 0;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;

		if (!(other instanceof X509Identity))
			return false;

		if ((_identity == null) || (((X509Identity) other)._identity == null)) {
			// one or the other is null
			if (_identity != ((X509Identity) other)._identity) {
				// they're not both null
				return false;
			}
		} else if (!_identity[0].equals(((X509Identity) other)._identity[0])) {
			// only check the first cert in the chain
			return false;
		}

		return true;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(_identity.length);
		out.writeObject(_type);
		out.writeObject(_mask);
		try {
			for (int i = 0; i < _identity.length; i++) {
				byte[] encoded = _identity[i].getEncoded();
				out.writeInt(encoded.length);
				out.write(encoded);

			}
		} catch (GeneralSecurityException e) {
			throw new IOException(e.getMessage());
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		int numCerts = in.readInt();
		String typeRead = in.readObject().toString();
		_type = IdentityType.valueOf(typeRead);
		_mask = (EnumSet<RWXCategory>) in.readObject();
		_identity = new X509Certificate[numCerts];
		try {
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			for (int i = 0; i < numCerts; i++) {
				byte[] encoded = new byte[in.readInt()];
				in.readFully(encoded);
				_identity[i] = (X509Certificate) cf
						.generateCertificate(new ByteArrayInputStream(encoded));
			}
		} catch (GeneralSecurityException e) {
			throw new IOException(e.getMessage());
		}

		if (_logger.isTraceEnabled())
			_logger.trace("read a type of " + typeRead + " for this x509 "
					+ _identity[0].getSubjectDN());

		if (_type == IdentityType.CONNECTION) {
			if (_logger.isDebugEnabled())
				_logger.debug("loading x509 identity with connection as type! -- "
						+ toString());
		}
	}

	@Override
	public boolean isPermitted(Identity identity)
			throws GeneralSecurityException {
		return this.equals(identity);
	}

	@Override
	public AclEntry sanitize() {
		return this;
	}

	@Override
	public boolean placeInUMask() {
		if (_type.equals(IdentityType.USER))
			return true;
		return false;
	}

	@Override
	public boolean checkRWXAccess(RWXCategory category) {
		if (!_mask.contains(category)) {
			if (_logger.isDebugEnabled())
				_logger.debug("Credential does not have " + category.toString()
						+ " access");
			return false;
		}
		return true;
	}
}
