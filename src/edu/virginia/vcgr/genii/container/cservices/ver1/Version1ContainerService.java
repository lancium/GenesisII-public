package edu.virginia.vcgr.genii.container.cservices.ver1;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import org.morgan.util.MacroUtils;

import edu.virginia.vcgr.genii.container.cservices.ContainerService;

class Version1ContainerService
{
	@XmlAttribute(name = "class", required = true)
	private String _className = null;
	
	@XmlElement(name = "property", nillable = true, required = false)
	private Collection<Version1Property> _properties =
		new LinkedList<Version1Property>();
	
	@SuppressWarnings("unchecked")
	final Class<? extends ContainerService> serviceClass(Properties macros) throws ClassNotFoundException
	{
		String className = MacroUtils.replaceMacros(macros, _className);
		Class<? extends ContainerService> serviceClass =
			(Class<? extends ContainerService>)Version1Upgrader.class.getClassLoader().loadClass(className);
		
		return serviceClass;
	}
	
	final Properties properties(Properties macros)
	{
		Properties ret = new Properties();
		for (Version1Property property : _properties)
			ret.setProperty(
				MacroUtils.replaceMacros(macros, property.name()),
				MacroUtils.replaceMacros(macros, property.value()));
		
		return ret;
	}
	
	@Override
	final public String toString()
	{
		return String.format("Class = %s", _className);
	}
}