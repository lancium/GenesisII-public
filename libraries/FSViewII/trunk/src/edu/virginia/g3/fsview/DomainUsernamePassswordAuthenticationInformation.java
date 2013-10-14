package edu.virginia.g3.fsview;

import javax.xml.bind.annotation.XmlAttribute;

public class DomainUsernamePassswordAuthenticationInformation extends AbstractFSViewAuthenticationInformation
{
	static final long serialVersionUID = 0L;

	@XmlAttribute(name = "domain")
	private String _domain;

	@XmlAttribute(name = "username")
	private String _userName;

	@XmlAttribute(name = "password")
	private String _password;

	@SuppressWarnings("unused")
	private DomainUsernamePassswordAuthenticationInformation()
	{
		this(null, null);
	}

	public DomainUsernamePassswordAuthenticationInformation(String domain, String username, String password)
	{
		super(FSViewAuthenticationInformationTypes.DomainUsernamePassword);

		if (username == null)
			username = "";

		if (password == null)
			password = "";

		if (domain == null) {
			int index = username.indexOf('\\');
			if (index > 0) {
				domain = username.substring(0, index);
				username = username.substring(index + 1);
			} else
				domain = "";
		}

		_domain = domain;
		_userName = username;
		_password = password;
	}

	public DomainUsernamePassswordAuthenticationInformation(String username, String password)
	{
		this(null, username, password);
	}

	final public String domain()
	{
		return _domain;
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
		return String.format("%s\\%s/*******", _domain, _userName);
	}
}