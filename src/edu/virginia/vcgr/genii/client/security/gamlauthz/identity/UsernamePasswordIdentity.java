package edu.virginia.vcgr.genii.client.security.gamlauthz.identity;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.security.GeneralSecurityException;

import org.apache.axis.message.MessageElement;

import edu.virginia.vcgr.genii.client.security.WSSecurityUtils;
import edu.virginia.vcgr.genii.client.security.gamlauthz.GamlCredential;
import edu.virginia.vcgr.genii.client.security.gamlauthz.assertions.AttributeInvalidException;

public class UsernamePasswordIdentity implements Identity, GamlCredential
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
		return "[Username-Token] Username: " + _userName; // + ", Token: " +
															// _token;
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

}
