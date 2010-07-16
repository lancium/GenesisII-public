package edu.virginia.vcgr.genii.container.cservices.conf;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.w3c.dom.Element;

@XmlRootElement(name = "container-service")
public class ContainerServiceConfiguration
{
	@XmlAttribute(name = "class", required = true)
	private String _className = null;
	
	@XmlElement(name = "property", nillable = true, required = false)
	private Collection<ContainerServiceProperty> _properties =
		new LinkedList<ContainerServiceProperty>();
	
	@XmlAnyElement
	private Collection<Element> _anyElements = new LinkedList<Element>();
	
	ContainerServiceConfiguration()
	{
		this(null, null);
	}
	
	public ContainerServiceConfiguration(String className,
		Properties properties)
	{
		_className = className;
		
		if (properties == null)
			properties = new Properties();
		
		for (Object key : properties.keySet())
		{
			String name = key.toString();
			_properties.add(new ContainerServiceProperty(
				name, properties.getProperty(name)));
		}
	}
	
	final public Properties properties()
	{
		Properties ret = new Properties();
		for (ContainerServiceProperty property : _properties)
			ret.setProperty(property.name(), property.value());
		
		return ret;
	}
	
	final public String className()
	{
		return _className;
	}
	
	final public Collection<Element> anyElements()
	{
		return _anyElements;
	}
}