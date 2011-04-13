package edu.virginia.vcgr.genii.client.wsrf.wsn;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMResult;

import org.apache.axis.message.MessageElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@XmlAccessorType(XmlAccessType.NONE)
public class NotificationMessageContents implements Cloneable, Serializable
{
	static final long serialVersionUID = 0L;
	
	@XmlAnyElement
	private Collection<Element> _any = new LinkedList<Element>();
	
	@XmlTransient
	private Element _additionalUserData = null;
	
	@XmlTransient
	private QName _originalName = null;
	
	@SuppressWarnings({ "unused", "unchecked" })
	private void afterUnmarshal(Unmarshaller u, Object parent)
	{
		if (parent != null && parent instanceof JAXBElement)
		{
			JAXBElement<? extends NotificationMessageContents> jaxbe =
				(JAXBElement<? extends NotificationMessageContents>)parent;
			_originalName = jaxbe.getName();
		}
		
		Iterator<Element> iter = _any.iterator();
		while (iter.hasNext())
		{
			Element e = iter.next();
			QName elementName = new QName(
				e.getNamespaceURI(), e.getLocalName());
			if (elementName.equals(AdditionalUserDataConstants.ELEMENT_QNAME))
			{
				_additionalUserData = e;
				iter.remove();
			}
		}
	}
	
	@SuppressWarnings("unused")
	private boolean beforeMarshal(Marshaller m) 
	{
		if (_additionalUserData != null)
			_any.add(_additionalUserData);
		return true;
	}

	final public Collection<Element> any()
	{
		return _any;
	}
	
	final public <Type extends AdditionalUserData> Type additionalUserData(
		Class<Type> type) throws JAXBException
	{
		if (_additionalUserData == null)
			return null;
		
		return AdditionalUserData.fromElement(type, _additionalUserData);
	}
	
	final public void additionalUserData(AdditionalUserData data)
		throws JAXBException
	{
		_additionalUserData = (data == null) ? null :
			AdditionalUserData.toMessageElement(data);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	final public MessageElement toAxisType() throws JAXBException
	{
		JAXBContext context = JAXBContext.newInstance(getClass());
		Marshaller m = context.createMarshaller();
		DOMResult result = new DOMResult();
		if (_originalName != null)
		{
			JAXBElement jaxbe = new JAXBElement(_originalName, getClass(), this);
			m.marshal(jaxbe, result);
		} else
			m.marshal(this, result);
		Node n = result.getNode();
		if (n instanceof Document)
			return new MessageElement(((Document)n).getDocumentElement());
		return new MessageElement((Element)result.getNode());
	}
	
	@Override
	public Object clone()
	{
		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(this);
			oos.close();
			baos.close();
			ByteArrayInputStream bais = new ByteArrayInputStream(
				baos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bais);
			return ois.readObject();
		}
		catch (IOException e)
		{
			throw new RuntimeException("Unable to clone message contents.", e);
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException("Unable to clone message contents.", e);
		}
	}
}