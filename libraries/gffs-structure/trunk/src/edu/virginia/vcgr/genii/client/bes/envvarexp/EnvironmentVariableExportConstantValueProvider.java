package edu.virginia.vcgr.genii.client.bes.envvarexp;

import javax.xml.bind.annotation.XmlAttribute;

class EnvironmentVariableExportConstantValueProvider implements EnvironmentVariableExportValueProvider
{
	@XmlAttribute(name = "value", required = true)
	private String _value = null;

	private EnvironmentVariableExportConstantValueProvider()
	{
		// For JAXB only.
	}

	@Override
	final public String value()
	{
		return _value;
	}
}