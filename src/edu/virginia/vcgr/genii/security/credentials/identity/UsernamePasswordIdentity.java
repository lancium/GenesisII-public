package edu.virginia.vcgr.genii.security.credentials.identity;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.Date;

import org.apache.axis.message.MessageElement;
import org.w3c.dom.Element;

import edu.virginia.vcgr.genii.algorithm.encryption.BCrypt;
import edu.virginia.vcgr.genii.security.VerbosityLevel;
import edu.virginia.vcgr.genii.security.XMLCompatible;
import edu.virginia.vcgr.genii.security.acl.AclEntry;
import edu.virginia.vcgr.genii.security.axis.WSSecurityUtils;
import edu.virginia.vcgr.genii.security.credentials.NuCredential;
import edu.virginia.vcgr.genii.security.faults.AttributeInvalidException;
import edu.virginia.vcgr.genii.security.identity.Identity;
import edu.virginia.vcgr.genii.security.identity.IdentityType;

/**
 * An Identity data-structure for UsernameToken credentials
 * 
 * @author dmerrill
 */
public class UsernamePasswordIdentity implements Identity, NuCredential, XMLCompatible
{
	static public final long serialVersionUID = 0L;

	protected String _userName;
	protected String _password;

	// zero-arg constructor for externalizable use only.
	public UsernamePasswordIdentity()
	{
	}

	public UsernamePasswordIdentity(String userName, String password)
	{
		_userName = userName;
		_password = password;
	}

	public UsernamePasswordIdentity(String userName, String password, boolean hash)
	{
		_userName = userName;
		// gensalt can be used to increase complexity
		if (hash && (password != null))
			_password = BCrypt.hashpw(password, BCrypt.gensalt());
		else
			_password = password;
	}

	public UsernamePasswordIdentity(MessageElement secToken) throws GeneralSecurityException
	{
		_userName = WSSecurityUtils.getNameTokenFromUTSecTokenRef(secToken);
		_password = WSSecurityUtils.getPasswordTokenFromUTSecTokenRef(secToken);
	}

	public String getUserName()
	{
		return _userName;
	}

	public String getPassword()
	{
		return _password;
	}

	/**
	 * Returns a URI (e.g., a WS-Security Token Profile URI) indicating the token type
	 */
	@Override
	public String getTokenType()
	{
		return WSSecurityUtils.PASSWORD_TEXT_URI;
	}

	@Override
	public String toString()
	{
		return describe(VerbosityLevel.HIGH);
	}

	@Override
	public String describe(VerbosityLevel verbosity)
	{
		return String.format("(Username-Token) (OWNER) %s", _userName);
	}

	@Override
	public Element convertToMessageElement() throws GeneralSecurityException
	{
		return WSSecurityUtils.makeUTSecTokenRef(_userName, _password);
	}

	@Override
	public X509Certificate[] getOriginalAsserter()
	{
		// Username tokens are not asserted by anything
		return null;
	}

	@Override
	public boolean equals(Object other)
	{
		if (other == null) {
			return false;
		}

		if (!(other instanceof UsernamePasswordIdentity)) {
			return false;
		}

		if (!_userName.equals(((UsernamePasswordIdentity) other)._userName)) {
			return false;
		}
		if (!_password.equals(((UsernamePasswordIdentity) other)._password)) {
			return false;
		}

		return true;
	}

	@Override
	public int hashCode()
	{
		if (_userName != null) {
			return _userName.hashCode();
		}

		return 0;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeUTF(_userName);
		out.writeUTF(_password);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
	{
		_userName = in.readUTF();
		_password = in.readUTF();
	}

	@Override
	public boolean isPermitted(Identity identity) throws GeneralSecurityException
	{
		// Dont grant access if current password is blank or null password
		if (_password.equals("") || _password == null)
			return false;

		if (identity == null)
			return false;

		if (!(identity instanceof UsernamePasswordIdentity))
			return false;

		if (!_userName.equals(((UsernamePasswordIdentity) identity)._userName))
			return false;

		if (!BCrypt.checkpw(((UsernamePasswordIdentity) identity)._password, _password)) {
			return false;
		}
		return true;
	}

	@Override
	public AclEntry sanitize()
	{
		// Temporarily allow return of password until we change set/get acls to be
		// out of resource properties. Should return hash if passwords are set properly
		// (With our up to date client)
		return new UsernamePasswordIdentity(_userName, _password);
	}

	@Override
	public boolean placeInUMask()
	{
		// Do not place usernames/passwords in newly created resources.
		return false;
	}

	@Override
	public IdentityType getType()
	{
		return IdentityType.USER;
	}

	@Override
	public void setType(IdentityType type)
	{
		// This does not do anything, as username password tokens are not transferred with
		// serialization.
		// So this flag would be lost.
	}

	@Override
	public void checkValidity(int delegationDepth, Date date) throws AttributeInvalidException
	{
		// the former version of this (without delegation depth) also had a null implementation.
	}
}
