package edu.virginia.vcgr.genii.client.nativeq;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.virginia.vcgr.genii.client.bes.ResourceOverrides;

@XmlAccessorType(XmlAccessType.NONE)
public class NativeQueueConfiguration implements Serializable, NativeQConstants
{
	static final long serialVersionUID = 0L;
	
	@XmlAttribute(name = "provider", required = true)
	private String _providerName = null;
	
	@XmlAttribute(name = "shared-directory", required = false)
	private String _sharedDirectory  = null;
	
	@XmlElement(namespace = NS, name = "trap-signal", required = false, 
		nillable = false)
	private Set<UnixSignals> _trapSignals = EnumSet.noneOf(UnixSignals.class);
	
	@XmlAnyElement
	private Collection<Element> _any = new LinkedList<Element>();
	
	@XmlTransient
	private Object _providerConfiguration = null;
	
	@SuppressWarnings("unused")
	private void afterUnmarshal(Unmarshaller u, Object parent)
		throws JAXBException, NativeQueueException, IOException
	{
		if (_any.size() > 1)
			throw new JAXBException(
				"A native queue configuration must have 0 or 1 provider configurations, not more.");
		
		if (_any.size() == 0)
			_providerConfiguration = null;
		else
		{
			NativeQueue queue = NativeQueues.getNativeQueue(_providerName);
			Class<?> configType = queue.providerConfigurationType();
			JAXBContext context = JAXBContext.newInstance(configType);
			u = context.createUnmarshaller();
			_providerConfiguration = u.unmarshal(_any.iterator().next(), 
				configType).getValue();
		}
		
		_any = null;
	}
	
	@SuppressWarnings("unused")
	private boolean beforeMarshal(Marshaller m) 
		throws JAXBException, ParserConfigurationException
	{
		_any = new LinkedList<Element>();
		
		if (_providerConfiguration != null)
		{
			JAXBContext context = JAXBContext.newInstance(
				_providerConfiguration.getClass());
			Marshaller mm = context.createMarshaller();
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.newDocument();
			mm.marshal(_providerConfiguration, doc);
			_any.add(doc.getDocumentElement());
		}
		
		return true;
	}
	
	private void writeObject(ObjectOutputStream out)
    	throws IOException
	{
		out.writeObject(_providerName);
		out.writeObject(_sharedDirectory);
		out.writeObject(_trapSignals);
		out.writeObject(_providerConfiguration);
	}

	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream in)
    	throws IOException, ClassNotFoundException
	{
		_providerName = (String)in.readObject();
		_sharedDirectory = (String)in.readObject();
		_trapSignals = (Set<UnixSignals>)in.readObject();
		_providerConfiguration = in.readObject();
		
		_any = null;
	}
	
	@SuppressWarnings("unused")
	private void readObjectNoData() 
    	throws ObjectStreamException
	{
		throw new StreamCorruptedException();
	}

	final public NativeQueueConnection connect(
		ResourceOverrides resourceOverrides, 
			File workingDirectory)
				throws NativeQueueException
	{
		NativeQueue queue = NativeQueues.getNativeQueue(_providerName);
		return queue.connect(resourceOverrides, workingDirectory,
			this, _providerConfiguration);
	}
	
	final public void providerName(String providerName)
	{
		_providerName = providerName;
	}
	
	final public void providerConfiguration(Object conf)
	{
		_providerConfiguration = conf;
	}
	
	final public File sharedDirectory()
	{
		return (_sharedDirectory == null) ? null : new File(_sharedDirectory);
	}
	
	final public void sharedDirectory(String directory)
	{
		_sharedDirectory = directory;
	}
	
	final public Set<UnixSignals> trapSignals()
	{
		return _trapSignals;
	}
}