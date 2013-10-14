package edu.virginia.g3.fsview;

import javax.xml.bind.annotation.XmlAttribute;

public class UsernamePasswordAuthenticationInformation extends AbstractFSViewAuthenticationInformation
{
	static final long serialVersionUID = 0L;

	@XmlAttribute(name = "username")
	private String _userName;

	@XmlAttribute(name = "password")
	private String _password;

	@SuppressWarnings("unused")
	private UsernamePasswordAuthenticationInformation()
	{
		this(null, null);
	}

	public UsernamePasswordAuthenticationInformation(String username, String password)
	{
		super(FSViewAuthenticationInformationTypes.UsernamePassword);

		if (username == null)
			username = "";
		if (password == null)
			password = "";

		_userName = username;
		_password = password;
	}

	final public String username()
	{
		return _userName;
	}

	final public String password()
	{
		return _password;
	}

	@Override
	final public String toString()
	{
		return String.format("%s/*******", _userName);
	}
}
