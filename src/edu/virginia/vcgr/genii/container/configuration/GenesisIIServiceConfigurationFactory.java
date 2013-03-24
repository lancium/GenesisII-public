package edu.virginia.vcgr.genii.container.configuration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.client.configuration.DeploymentName;
import edu.virginia.vcgr.genii.client.configuration.HierarchicalDirectory;
import edu.virginia.vcgr.genii.client.configuration.Installation;

public class GenesisIIServiceConfigurationFactory
{
	static private Map<Class<?>, GenesisIIServiceConfiguration> _configurationMap = new HashMap<Class<?>, GenesisIIServiceConfiguration>();

	static private GenesisIIServiceConfiguration loadConfiguration(Class<?> serviceClass)
	{
		JAXBGenesisIIServiceConfiguration jaxbConf = null;
		CompositeGenesisIIServiceConfiguration composite = new CompositeGenesisIIServiceConfiguration(serviceClass);

		HierarchicalDirectory dir = Installation.getDeployment(new DeploymentName()).getConfigurationDirectory();
		if ((dir != null) && dir.exists()) {
			dir = dir.lookupDirectory("service-configs");
			if ((dir != null) && (dir.exists())) {
				File configFile = dir.lookupFile(serviceClass.getName());
				if ((configFile != null) && configFile.exists()) {
					try {
						JAXBContext context = JAXBContext.newInstance(composite.jaxbServiceConfigurationClass());
						Unmarshaller u = context.createUnmarshaller();
						jaxbConf = u.unmarshal(new StreamSource(configFile), composite.jaxbServiceConfigurationClass())
							.getValue();
					} catch (JAXBException e) {
						throw new ConfigurationException(String.format("Unable to deserialize %s into %s for service %s.",
							configFile, composite.jaxbServiceConfigurationClass(), serviceClass), e);
					}
				}
			}
		}

		if (jaxbConf != null) {
			jaxbConf.setParent(composite);
			return jaxbConf;
		} else
			return composite;
	}

	static public GenesisIIServiceConfiguration configurationFor(Class<?> serviceClass)
	{
		GenesisIIServiceConfiguration conf;

		synchronized (_configurationMap) {
			conf = _configurationMap.get(serviceClass);
			if (conf == null)
				_configurationMap.put(serviceClass, conf = loadConfiguration(serviceClass));
		}

		return conf;
	}
}