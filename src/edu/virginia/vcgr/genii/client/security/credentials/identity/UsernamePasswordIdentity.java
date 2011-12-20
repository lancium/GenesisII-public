package edu.virginia.vcgr.genii.client.security.credentials.identity;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.security.GeneralSecurityException;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.client.security.VerbosityLevel;
import edu.virginia.vcgr.genii.client.security.WSSecurityUtils;
import edu.virginia.vcgr.genii.client.security.authz.acl.AclEntry;
import edu.virginia.vcgr.genii.client.security.credentials.GIICredential;
import edu.virginia.vcgr.genii.client.security.credentials.assertions.AttributeInvalidException;
import edu.virginia.vcgr.security.BCrypt;

/**
 * An Identity data-structure for UsernameToken credentials
 * 
 * @author dgm4d
 *
 */
public class UsernamePasswordIdentity implements Identity, GIICredential
{

	static public final long serialVersionUID = 0L;

	protected String _userName;
	protected String _password;

	// zero-arg contstructor for externalizable use only!
	public UsernamePasswordIdentity()
	{
	}

	public UsernamePasswordIdentity(String userName, String password)
	{
		_userName = userName;
		_password = password;
	}
	
	public UsernamePasswordIdentity(String userName, String password, boolean hash){
		_userName = userName;
		//gensalt can be used to increase complexity
		if(hash && (password != null))
			_password = BCrypt.hashpw(password, BCrypt.gensalt());
		else
			_password = password;
			
		
	}

	public UsernamePasswordIdentity(MessageElement secToken)
			throws GeneralSecurityException
	{
		_userName = WSSecurityUtils.getNameTokenFromUTSecTokenRef(secToken);
		_password = WSSecurityUtils.getPasswordTokenFromUTSecTokenRef(secToken);
	}

	/**
	 * Returns a URI (e.g., a WS-Security Token Profile URI) indicating the
	 * token type
	 */
	public String getTokenType()
	{
		return WSSecurityUtils.PASSWORD_TEXT_URI;
	}

	public String toString()
	{
		return describe(VerbosityLevel.HIGH);
	}
	
	@Override
	public String describe(VerbosityLevel verbosity)
	{
		return String.format("(Username-Token) %s", _userName);
	}

	public MessageElement toMessageElement() throws GeneralSecurityException
	{
		return WSSecurityUtils.makeUTSecTokenRef(_userName, _password);
	}

	public X509Certificate[] getAssertingIdentityCertChain()
	{
		// Username tokens are not asserted by anything
		return null;
	}

	@Override
	public boolean equals(Object other)
	{
		if (other == null)
		{
			return false;
		}

		if (!(other instanceof UsernamePasswordIdentity))
		{
			return false;
		}

		if (!_userName.equals(((UsernamePasswordIdentity) other)._userName))
		{
			return false;
		}
		if (!_password.equals(((UsernamePasswordIdentity) other)._password))
		{
			return false;
		}

		return true;
	}
	
	@Override
	public int hashCode() 
	{
		if (_userName != null) 
		{
			return _userName.hashCode();
		}
		
		return 0;
	}
	
	public String getUserName()
	{
		return _userName;
	}

	public String getPassword()
	{
		return _password;
	}

	public void checkValidity(Date date) throws AttributeInvalidException
	{
	}

	public void writeExternal(ObjectOutput out) throws IOException
	{
		out.writeUTF(_userName);
		out.writeUTF(_password);
	}

	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException
	{
		_userName = in.readUTF();
		_password = in.readUTF();
	}

	@Override
	public boolean isPermitted(Identity identity)
			throws GeneralSecurityException {

		//Dont grant access if current password is blank or null password
		if (_password.equals("") || _password == null)
			return false;

		if (identity == null)
			return false;
	
		if (!(identity instanceof UsernamePasswordIdentity))
			return false;

		if (!_userName.equals(((UsernamePasswordIdentity) identity)._userName))
			return false;
		
		if (!BCrypt.checkpw(
				((UsernamePasswordIdentity) identity)._password,
				_password))
		{
			return false;
		}

		
		return true;

	}

	@Override
	public AclEntry sanitize() {
		//Temporarily allow return of password until we change set/get acls to be
		//out of resource properties.  Should return hash if passwords are set properly 
		//(With our up to date client)
		return new UsernamePasswordIdentity(_userName, _password);
	}

}
