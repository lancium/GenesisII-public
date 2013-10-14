package edu.virginia.vcgr.externalapp;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;

import org.w3c.dom.Element;

class ApplicationRegistration
{
	@XmlAttribute(name = "type", required = true)
	private ApplicationRegistrationTypes _registrationType = null;

	@XmlAttribute(name = "factory-class", required = true)
	private String _factoryClassName = null;

	@XmlAnyElement
	private Element _configuration = null;

	final ApplicationRegistrationTypes registrationType()
	{
		return _registrationType;
	}

	final ExternalApplication createApplication() throws ClassNotFoundException, InstantiationException, IllegalAccessException
	{
		Class<?> factoryClass = Class.forName(_factoryClassName);
		ExternalApplicationFactory factory = (ExternalApplicationFactory) factoryClass.newInstance();
		return factory.createApplication(_configuration);
	}
}