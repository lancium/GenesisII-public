package edu.virginia.vcgr.genii.client.wsrf.wsn;

import java.io.Serializable;
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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.dom.DOMResult;

import org.apache.axis.message.MessageElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@XmlRootElement(namespace = AdditionalUserDataConstants.NS, name = AdditionalUserDataConstants.ELEMENT_NAME)
public class AdditionalUserData implements Serializable
{
	static final long serialVersionUID = 0L;

	@XmlAnyElement
	private Collection<Element> _items = new LinkedList<Element>();

	@XmlAnyAttribute
	private Map<String, String> _attributes = new HashMap<String, String>();

	final public Collection<Element> items()
	{
		return _items;
	}

	final public Map<String, String> attributes()
	{
		return _attributes;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	static public <Type extends AdditionalUserData> MessageElement toMessageElement(Type value) throws JAXBException
	{
		JAXBContext context = JAXBContext.newInstance(value.getClass());
		Marshaller m = context.createMarshaller();
		DOMResult result = new DOMResult();
		JAXBElement jaxbe = new JAXBElement(AdditionalUserDataConstants.ELEMENT_QNAME, value.getClass(), value);
		m.marshal(jaxbe, result);
		return new MessageElement(((Document) result.getNode()).getDocumentElement());
	}

	static public <Type extends AdditionalUserData> Type fromElement(Class<Type> type, Element me) throws JAXBException
	{
		JAXBContext context = JAXBContext.newInstance(type);
		Unmarshaller u = context.createUnmarshaller();
		Object obj = u.unmarshal(me, type).getValue();
		return type.cast(obj);
	}
}