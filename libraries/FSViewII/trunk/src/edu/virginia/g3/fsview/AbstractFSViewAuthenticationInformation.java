package edu.virginia.g3.fsview;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlTransient;

public class AbstractFSViewAuthenticationInformation implements FSViewAuthenticationInformation, Serializable
{
	static final long serialVersionUID = 0L;

	@XmlTransient
	private FSViewAuthenticationInformationTypes _authType;

	@SuppressWarnings("unused")
	private AbstractFSViewAuthenticationInformation()
	{
		this(FSViewAuthenticationInformationTypes.Anonymous);
	}

	protected AbstractFSViewAuthenticationInformation(FSViewAuthenticationInformationTypes authType)
	{
		_authType = authType;
	}

	@Override
	final public FSViewAuthenticationInformationTypes authenticationType()
	{
		return _authType;
	}
}