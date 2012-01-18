package edu.virginia.vcgr.genii.security.credentials.identity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.security.GeneralSecurityException;
import java.security.cert.*;

import java.util.*;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.client.security.authz.acl.AclEntry;
import edu.virginia.vcgr.genii.client.security.authz.rwx.RWXCategory;

import edu.virginia.vcgr.genii.client.security.SecurityUtils;
import edu.virginia.vcgr.genii.security.VerbosityLevel;
import edu.virginia.vcgr.genii.security.WSSecurityUtils;
import edu.virginia.vcgr.genii.security.X500PrincipalUtilities;
import edu.virginia.vcgr.genii.security.credentials.assertions.*;

/**
 * An Identity/SignedAssertion wrapper for X.509 identities
 * 
 * @author dgm4d
 *
 */
public class X509Identity implements HolderOfKeyIdentity, SignedAssertion
{

	static public final long serialVersionUID = 0L;

	protected X509Certificate[] _identity;
	private IdentityType _type = IdentityType.OWNER;
	private EnumSet<RWXCategory> _mask = 
			EnumSet.of(RWXCategory.READ, RWXCategory.WRITE, RWXCategory.EXECUTE);

	// zero-arg contstructor for externalizable use only!
	public X509Identity()
	{
	}

	public X509Identity(X509Certificate[] identity)
	{
		_identity = identity;
	}
	
	public X509Identity(X509Certificate[] identity, IdentityType type)
	{
		this(identity);
		_type = type;
		
	}

	public X509Identity(MessageElement secRef) throws GeneralSecurityException
	{
		_identity = WSSecurityUtils.getChainFromPkiPathSecTokenRef(secRef);
	}

	public X509Certificate[] getAssertingIdentityCertChain()
	{
		// X509 certificates assert themselves via their own
		// corresponding private key
		return _identity;
	}

	/**
	 * Returns a URI (e.g., a WS-Security Token Profile URI) indicating the
	 * token type
	 */
	public String getTokenType()
	{
		return WSSecurityUtils.X509PKIPathv1_URI;
	}

	public MessageElement toMessageElement() throws GeneralSecurityException
	{
		return WSSecurityUtils.makePkiPathSecTokenRef(_identity);
	}

	/**
	 * Returns the primary attribute that is being asserted
	 */
	public Attribute getAttribute()
	{
		return new IdentityAttribute(this);
	}

	/**
	 * Returns the certchain of the identity authorized to use this assertion
	 * (same as the asserter)
	 */
	public X509Certificate[] getAuthorizedIdentity()
	{
		return _identity;
	}

	/**
	 * Verify the assertion. It is verified if all signatures successfully
	 * authenticate the signed-in authorizing identities
	 */
	public void validateAssertion() throws GeneralSecurityException
	{
		edu.virginia.vcgr.genii.client.security.x509.CertTool.loadBCProvider();

		SecurityUtils.validateCertPath(_identity, false);
	}

	/**
	 * Checks that the attribute is time-valid with respect to the supplied date
	 * and any delegation depth requirements are met by the supplied
	 * delegationDepth.
	 */
	public void checkValidity(Date date) throws AttributeInvalidException
	{

		try
		{
			for (X509Certificate cert : getAssertingIdentityCertChain())
			{
				cert.checkValidity(date);
			}
		}
		catch (CertificateException e)
		{
			throw new AttributeInvalidException(
					"Security attribute asserting identity contains an invalid certificate: "
							+ e.getMessage(), e);
		}
	}

	public String toString()
	{
		return describe(VerbosityLevel.HIGH);
	}
	
	@Override
	public String describe(VerbosityLevel verbosity)
	{
		switch (verbosity) {
		case HIGH:
			return String.format("(X509Identity) \"%s\"  [%2$tD %2$tT, %3$tD %3$tT]", 
					X500PrincipalUtilities.describe(
						_identity[0].getSubjectX500Principal(), verbosity),
					_identity[0].getNotBefore(), 
					_identity[0].getNotAfter());

		case MEDIUM:
			return String.format("\"%s\"  [%2$tD %2$tT, %3$tD %3$tT]", 
					X500PrincipalUtilities.describe(
						_identity[0].getSubjectX500Principal(), verbosity),
					_identity[0].getNotBefore(), 
					_identity[0].getNotAfter());

		default:
			return ("(" + _type.toString() + ") " + X500PrincipalUtilities.describe(
					_identity[0].getSubjectX500Principal(), verbosity));
		}
	}

	@Override
	public int hashCode() 
	{
		if ((_identity != null) && (_identity.length != 0))
			return _identity[0].hashCode();
		
		return 0;
	}
	
	public boolean equals(Object other)
	{
		if (other == null)
			return false;

		if (!(other instanceof X509Identity))
			return false;
		
		if ((_identity == null) || (((X509Identity) other)._identity == null))
		{
			// one or the other is null
			if (_identity != ((X509Identity) other)._identity)
			{
				// they're not both null
				return false;
			}
		} else if (!_identity[0].equals(((X509Identity) other)._identity[0]))
		{
			// only check the first cert in the chain
			return false;
		}

		return true;
	}

	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeInt(_identity.length);
		out.writeObject(_type);
		out.writeObject(_mask);
		try
		{
			for (int i = 0; i < _identity.length; i++)
			{
				byte[] encoded = _identity[i].getEncoded();
				out.writeInt(encoded.length);
				out.write(encoded);
				
			}
		}
		catch (GeneralSecurityException e)
		{
			throw new IOException(e.getMessage());
		}
	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException
	{
		int numCerts = in.readInt();
		_type = (IdentityType) in.readObject();
		_mask = (EnumSet<RWXCategory>) in.readObject();
		_identity = new X509Certificate[numCerts];
		try
		{
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			for (int i = 0; i < numCerts; i++)
			{
				byte[] encoded = new byte[in.readInt()];
				in.readFully(encoded);
				_identity[i] =
						(X509Certificate) cf
								.generateCertificate(new ByteArrayInputStream(
										encoded));
			}
		}
		catch (GeneralSecurityException e)
		{
			throw new IOException(e.getMessage());
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
		
		if(_type.equals(IdentityType.OWNER))
			return true;
		
		return false;
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
	public boolean checkAccess(RWXCategory category)
			throws GeneralSecurityException {
		if (!_mask.contains(category))
			throw new GeneralSecurityException(
					"Credential does not have " + 
			category.toString() + " access");
		
		return true;
	}

	@Override
	public void setMask(EnumSet<RWXCategory> perms) {
		_mask = perms;
	}



}
