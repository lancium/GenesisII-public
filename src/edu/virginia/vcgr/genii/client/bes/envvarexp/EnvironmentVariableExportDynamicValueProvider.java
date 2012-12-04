package edu.virginia.vcgr.genii.client.bes.envvarexp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
class EnvironmentVariableExportDynamicValueProvider
	implements EnvironmentVariableExportValueProvider
{
	private EnvironmentVariableExportValueProvider _realProvider;
	
	@XmlAttribute(name = "class", required = true)
	private void setClassName(String className)
		throws InstantiationException, IllegalAccessException, 
			ClassNotFoundException
	{
		_realProvider = (EnvironmentVariableExportValueProvider)(
			Class.forName(className).newInstance());
	}
	
	@SuppressWarnings("unused")
	private String getClassName()
	{
		if (_realProvider != null)
			return _realProvider.getClass().getName();
		
		return null;
	}
	
	private EnvironmentVariableExportDynamicValueProvider()
	{
		// For JAXB only.
	}
	
	@Override
	final public String value()
	{
		return _realProvider.value();
	}
}