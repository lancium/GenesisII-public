package edu.virginia.vcgr.genii.client.bes.envvarexp;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.client.bes.BESConstructionParameters;
import edu.virginia.vcgr.genii.client.configuration.DeploymentName;
import edu.virginia.vcgr.genii.client.configuration.Installation;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(namespace = EnvironmentVariableExportConstants.NAMESPACE, name = "environment-export")
public class EnvironmentExport
{
	static private EnvironmentExport CONTAINER_EXPORT = null;

	private EnvironmentExport _parent = null;

	private Map<String, EnvironmentVariableExport> _variables = new HashMap<String, EnvironmentVariableExport>();

	@XmlElement(namespace = EnvironmentVariableExportConstants.NAMESPACE, name = "environment-variable", required = false, nillable = false)
	private void setEnvironmentVariables(EnvironmentVariableExport[] variables)
	{
		_variables.clear();

		if (variables != null) {
			for (EnvironmentVariableExport variable : variables)
				_variables.put(variable.variableName(), variable);
		}
	}

	private EnvironmentExport()
	{
		// For JAXB Only.
	}

	private void setParent(EnvironmentExport parent)
	{
		_parent = parent;
	}

	final public Set<String> keySet()
	{
		Set<String> ret = new HashSet<String>();
		if (_parent != null)
			ret.addAll(_parent.keySet());
		ret.addAll(_variables.keySet());
		return ret;
	}

	final public String value(String variableName)
	{
		EnvironmentVariableExport variable = _variables.get(variableName);
		if (variable != null)
			return variable.value();

		if (_parent != null)
			return _parent.value(variableName);

		return null;
	}

	synchronized static private EnvironmentExport containerExport()
	{
		try {
			if (CONTAINER_EXPORT == null) {
				File file = Installation.getDeployment(new DeploymentName()).getConfigurationFile(
					EnvironmentVariableExportConstants.GLOBAL_CONFIG_FILE_NAME);
				if (file.exists()) {
					JAXBContext context = JAXBContext.newInstance(EnvironmentExport.class);
					Unmarshaller u = context.createUnmarshaller();

					CONTAINER_EXPORT = (EnvironmentExport) (u.unmarshal(file));
				} else
					CONTAINER_EXPORT = new EnvironmentExport();
			}

			return CONTAINER_EXPORT;
		} catch (JAXBException e) {
			throw new ConfigurationException("Unable to deserialize environment export.", e);
		}
	}

	static public EnvironmentExport besExport(BESConstructionParameters besConsParms)
	{
		EnvironmentExport besExport = besConsParms.environmentExport();
		if (besExport == null)
			return containerExport();

		EnvironmentExport ret = new EnvironmentExport();
		ret._variables.putAll(besExport._variables);
		ret.setParent(containerExport());
		return ret;
	}
}