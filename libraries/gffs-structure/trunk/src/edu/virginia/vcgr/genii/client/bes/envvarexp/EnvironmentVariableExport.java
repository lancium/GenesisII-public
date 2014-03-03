package edu.virginia.vcgr.genii.client.bes.envvarexp;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = EnvironmentVariableExportConstants.NAMESPACE, name = "environment-variable")
@XmlAccessorType(XmlAccessType.NONE)
class EnvironmentVariableExport
{
	@XmlAttribute(name = "name")
	private String _variableName = null;

	private EnvironmentVariableExportValueProvider _valueProvider = null;

	@XmlElement(namespace = EnvironmentVariableExportConstants.NAMESPACE, name = "constant-value", required = false)
	private void setConstantValue(EnvironmentVariableExportConstantValueProvider value)
	{
		_valueProvider = value;
	}

	@SuppressWarnings("unused")
	private EnvironmentVariableExportConstantValueProvider getConstantValue()
	{
		if (_valueProvider != null && _valueProvider instanceof EnvironmentVariableExportConstantValueProvider)
			return ((EnvironmentVariableExportConstantValueProvider) _valueProvider);

		return null;
	}

	@XmlElement(namespace = EnvironmentVariableExportConstants.NAMESPACE, name = "dynamic-value", required = false)
	private void setDynamicValue(EnvironmentVariableExportDynamicValueProvider value)
	{
		_valueProvider = value;
	}

	@SuppressWarnings("unused")
	private EnvironmentVariableExportDynamicValueProvider getDynamicValue()
	{
		if (_valueProvider != null && _valueProvider instanceof EnvironmentVariableExportDynamicValueProvider)
			return ((EnvironmentVariableExportDynamicValueProvider) _valueProvider);

		return null;
	}

	private EnvironmentVariableExport()
	{
		// For JAXB only.
	}

	final public String variableName()
	{
		return _variableName;
	}

	final public String value()
	{
		if (_valueProvider != null)
			return _valueProvider.value();

		return null;
	}

	@Override
	final public String toString()
	{
		return String.format("%s=%s", variableName(), value());
	}
}