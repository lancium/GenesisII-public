package edu.virginia.vcgr.genii.client.common;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.axis.message.MessageElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import edu.virginia.vcgr.genii.client.bes.BESConstructionParameters;
import edu.virginia.vcgr.genii.container.bes.GeniiBESServiceImpl;

@XmlRootElement(namespace = ConstructionParameters.CONSTRUCTION_PARAMTERS_NS,
	name = ConstructionParameters.CONSTRUCTION_PARAMETERS_NAME)
public class ConstructionParameters implements Serializable
{
	static final long serialVersionUID = 0L;
	
	static final public String CONSTRUCTION_PARAMTERS_NS =
		"http://vcgr.cs.virginia.edu/construction-parameters";
	
	static final public String CONSTRUCTION_PARAMETERS_NAME =
		"construction-parameters";
	
	static final public QName CONSTRUCTION_PARAMTERS_QNAME =
		new QName(CONSTRUCTION_PARAMTERS_NS, CONSTRUCTION_PARAMETERS_NAME);
	
	static private Map<Class<?>, Class<? extends ConstructionParameters>> _typeMap =
		new HashMap<Class<?>, Class<? extends ConstructionParameters>>();
	static private Map<Class<? extends ConstructionParameters>, JAXBContext> _contextMap =
		new HashMap<Class<? extends ConstructionParameters>, JAXBContext>();
	
	static private Class<? extends ConstructionParameters> findConstructionParameterType(
		Class<?> serviceClass)
	{
		while (serviceClass != null && !serviceClass.equals(Object.class))
		{
			ConstructionParametersType cpt = serviceClass.getAnnotation(
				ConstructionParametersType.class);
			if (cpt != null)
				return cpt.value();
			
			serviceClass = serviceClass.getSuperclass();
		}
		
		return null;
	}
	
	static private Class<? extends ConstructionParameters> getConstructionParameterType(
		Class<?> serviceClass)
	{
		Class<? extends ConstructionParameters> ret = null;
		
		synchronized(_typeMap)
		{
			ret = _typeMap.get(serviceClass);
		}
		
		if (ret == null)
		{
			ret = findConstructionParameterType(serviceClass);
			if (ret == null)
				throw new IllegalArgumentException(String.format(
					"Service class %s does not have the required ConstructionParametersType annotation.",
					serviceClass));
			synchronized(_typeMap)
			{
				_typeMap.put(serviceClass, ret);
			}
		}
		
		return ret;
	}
	
	static public ConstructionParameters instantiateDefault(
		Class<?> serviceClass)
	{
		Class<? extends ConstructionParameters> pType = null;
		
		try
		{
			pType = getConstructionParameterType(serviceClass);
			return pType.newInstance();
		}
		catch (IllegalAccessException iae)
		{
			throw new RuntimeException(String.format(
				"Construction parameter type %s does not " +
				"have a public no-arg constructor.", pType), iae);
		}
		catch (InstantiationException e)
		{
			throw new RuntimeException(String.format(
				"Construction parameter type %s does not " +
				"have a public no-arg constructor.", pType), e);
		}
	}
	
	static private JAXBContext getContext(
		Class<? extends ConstructionParameters> type) throws JAXBException
	{
		JAXBContext context;
		
		synchronized(_contextMap)
		{
			context = _contextMap.get(type);
			if (context == null)
			{
				context = JAXBContext.newInstance(type);
				_contextMap.put(type, context);
			}
		}
		
		return context;
	}
	
	static public ConstructionParameters deserializeConstructionParameters(
		Class<?> serviceClass, Node serializedContent) throws JAXBException
	{
		Class<? extends ConstructionParameters> pType = 
			getConstructionParameterType(serviceClass);
		JAXBContext context = getContext(pType);
		
		Unmarshaller u = context.createUnmarshaller();
		return u.unmarshal(serializedContent, pType).getValue();
	}
	
	static public ConstructionParameters deserializeConstructionParameters(
		Class<?> serviceClass, InputStream in) throws JAXBException
	{
		Class<? extends ConstructionParameters> pType = 
			getConstructionParameterType(serviceClass);
		JAXBContext context = getContext(pType);
		
		Unmarshaller u = context.createUnmarshaller();
		return u.unmarshal(new StreamSource(in), pType).getValue();
	}
	
	static public ConstructionParameters deserializeConstructionParameters(
		InputStream in) throws JAXBException
	{
		JAXBContext context = getContext(BESConstructionParameters.class);
		Unmarshaller u = context.createUnmarshaller();
		return u.unmarshal(new StreamSource(in),
			BESConstructionParameters.class).getValue();
	}
	
	@XmlAttribute(name = "time-to-live", required = false)
	private Long _timeToLive = null;
	
	@XmlAttribute(name = "human-name", required = false)
	private String _humanName = null;
	
	@XmlAnyElement
	private Collection<Element> _any = new LinkedList<Element>();
	
	@XmlAnyAttribute
	private Map<QName, Object> _anyAttributes = new HashMap<QName, Object>();
	
	public ConstructionParameters()
	{
	}
	
	final public Long timeToLive()
	{
		return _timeToLive;
	}
	
	final public void timeToLive(Long timeToLive)
	{
		_timeToLive = timeToLive;
	}
	
	final public String humanName()
	{
		return _humanName;
	}
	
	final public void humanName(String humanName)
	{
		_humanName = humanName;
	}
	
	final public Collection<Element> any()
	{
		return _any;
	}
	
	final public Map<QName, Object> anyAttributes()
	{
		return _anyAttributes;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	final public MessageElement serializeToMessageElement()
		throws RemoteException
	{
		try
		{
			JAXBContext context = getContext(getClass());
			Marshaller m = context.createMarshaller();
			JAXBElement jbe = new JAXBElement(
				ConstructionParameters.CONSTRUCTION_PARAMTERS_QNAME,
				getClass(), this);
			DOMResult result = new DOMResult();
			m.marshal(jbe, result);
			return new MessageElement(
				((Document)result.getNode()).getDocumentElement());
		}
		catch (JAXBException e)
		{
			throw new RemoteException(
				"Unable to serialize construction parameters.", e);
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	final public void serialize(OutputStream sink)
		throws JAXBException
	{
		JAXBContext context = getContext(getClass());
		Marshaller m = context.createMarshaller();
		m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
		JAXBElement jbe = new JAXBElement(
			ConstructionParameters.CONSTRUCTION_PARAMTERS_QNAME,
			getClass(), this);
		m.marshal(jbe, sink);
	}
	
	static public void main(String []args) throws Throwable
	{
		FileInputStream fin = new FileInputStream("/Users/morgan/BatchSystems/PBS/queue.xml");
		ConstructionParameters cparams = ConstructionParameters.deserializeConstructionParameters(fin);
		MessageElement me = cparams.serializeToMessageElement();
		System.out.println(me);
		cparams = ConstructionParameters.deserializeConstructionParameters(GeniiBESServiceImpl.class, me);
		cparams = null;
	}
}