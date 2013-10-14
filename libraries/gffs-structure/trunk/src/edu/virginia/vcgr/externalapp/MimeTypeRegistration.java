package edu.virginia.vcgr.externalapp;

import javax.xml.bind.annotation.XmlAttribute;

class MimeTypeRegistration extends DefaultRegistration
{
	@XmlAttribute(name = "name", required = true)
	private String _mimeType;

	final String mimeType()
	{
		return _mimeType;
	}
}