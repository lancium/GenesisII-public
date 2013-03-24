package edu.virginia.vcgr.genii.container.cservices.conf;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Element;

import edu.virginia.vcgr.genii.client.configuration.HierarchicalDirectory;
import edu.virginia.vcgr.genii.client.utils.file.ExtensionFileFilter;
import edu.virginia.vcgr.genii.container.cservices.ContainerService;

@XmlRootElement(name = "container-service")
public class ContainerServiceConfiguration
{
	static private Log _logger = LogFactory.getLog(ContainerServiceConfiguration.class);

	@XmlTransient
	private File _sourceFile;

	@XmlTransient
	private Class<? extends ContainerService> _serviceClass = null;

	@XmlAttribute(name = "class", required = true)
	private String _className = null;

	@XmlElement(name = "property", nillable = true, required = false)
	private Collection<ContainerServiceProperty> _properties = new LinkedList<ContainerServiceProperty>();

	@XmlAnyElement
	private Collection<Element> _anyElements = new LinkedList<Element>();

	ContainerServiceConfiguration()
	{
		this(null, null);
	}

	public ContainerServiceConfiguration(Class<? extends ContainerService> serviceClass, Properties properties)
	{
		_serviceClass = serviceClass;
		_className = (_serviceClass == null) ? null : _serviceClass.getName();

		if (properties == null)
			properties = new Properties();

		for (Object key : properties.keySet()) {
			String name = key.toString();
			_properties.add(new ContainerServiceProperty(name, properties.getProperty(name)));
		}
	}

	final public File configurationFile()
	{
		return _sourceFile;
	}

	final public Properties properties()
	{
		Properties ret = new Properties();
		for (ContainerServiceProperty property : _properties)
			ret.setProperty(property.name(), property.value());

		return ret;
	}

	@SuppressWarnings("unchecked")
	final public Class<? extends ContainerService> serviceClass() throws ClassNotFoundException
	{
		if (_serviceClass == null) {
			_serviceClass = (Class<? extends ContainerService>) ContainerServiceConfiguration.class.getClassLoader().loadClass(
				_className);
		}

		return _serviceClass;
	}

	final public Collection<Element> anyElements()
	{
		return _anyElements;
	}

	final public ContainerService instantiate() throws SecurityException, NoSuchMethodException, IllegalArgumentException,
		InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException
	{
		ContainerService ret = null;
		Constructor<? extends ContainerService> cons;
		serviceClass();

		Element[] any;
		if (_anyElements == null)
			any = new Element[0];
		else {
			any = new Element[_anyElements.size()];
			_anyElements.toArray(any);
		}

		try {
			cons = _serviceClass.getConstructor(Element[].class);
			ret = cons.newInstance((Object) any);
		} catch (NoSuchMethodException nsme1) {
			if (any.length > 1)
				throw new NoSuchMethodException(String.format("Unable to find suitable constructor for "
					+ "service %s defined in file %s.", _serviceClass, _sourceFile));
			try {
				cons = _serviceClass.getConstructor(Element.class);
				if (any.length == 0)
					ret = cons.newInstance((Object) null);
				else
					ret = cons.newInstance(any[0]);
			} catch (NoSuchMethodException nsme2) {
				cons = _serviceClass.getConstructor();
				ret = cons.newInstance();
			}
		}

		if (ret != null)
			ret.setProperties(properties());

		return ret;
	}

	static public Collection<ContainerServiceConfiguration> loadConfigurations(HierarchicalDirectory sourceDirectory)
		throws IOException
	{
		Collection<ContainerServiceConfiguration> ret = new LinkedList<ContainerServiceConfiguration>();
		JAXBContext context;
		Unmarshaller unmarshaller;

		try {
			context = JAXBContext.newInstance(ContainerServiceConfiguration.class);
			unmarshaller = context.createUnmarshaller();
		} catch (JAXBException e) {
			throw new IOException("Unable to load container services.", e);
		}

		for (File sourceFile : sourceDirectory.listFiles(ExtensionFileFilter.XML)) {
			try {
				ContainerServiceConfiguration conf = (ContainerServiceConfiguration) unmarshaller.unmarshal(sourceFile);
				conf._sourceFile = sourceFile;
				ret.add(conf);
			} catch (JAXBException e) {
				_logger.error(
					String.format("Unable to load container service configuration " + "from file \"%s\".", sourceFile), e);
			}
		}

		return ret;
	}
}