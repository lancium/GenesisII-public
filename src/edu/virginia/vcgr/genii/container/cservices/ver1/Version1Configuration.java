package edu.virginia.vcgr.genii.container.cservices.ver1;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "container-services")
class Version1Configuration
{
	@XmlElement(name = "variable", nillable = true, required = false)
	private Collection<Version1Variable> _variables =
		new LinkedList<Version1Variable>();
	
	@XmlElement(name = "container-service", nillable = true, required = false)
	private Collection<Version1ContainerService> _services =
		new LinkedList<Version1ContainerService>();
	
	final Properties variables()
	{
		Properties ret = new Properties();
		for (Version1Variable variable : _variables)
			ret.setProperty(variable.name(), variable.value());

		return ret;
	}
	
	final Collection<Version1ContainerService> services()
	{
		return _services;
	}
}