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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.namespace.QName;
import javax.xml.transform.dom.DOMResult;

import org.apache.axis.message.MessageElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;

@XmlAccessorType(XmlAccessType.NONE)
public class NotificationMessageContents implements Cloneable, Serializable
{
	static final long serialVersionUID = 0L;

	@XmlAnyElement
	private Collection<Element> _any = new LinkedList<Element>();

	@XmlTransient
	private Element _additionalUserData = null;

	/*
	 * This includes all the attributes that cannot be properly serialized or deserialized by JAXB.
	 * Information stored within this array do not travel as message content, rather these traveled
	 * as part of the extension of the notification message (in "any" field of Notify class). As the
	 * above suggests, this is not an ideal way of passing attributes. Hence, we should change how
	 * these attributes are propagated if we find any better alternative.
	 */
	transient private MessageElement[] _additionalAttributes;

	/*
	 * Publisher blocking is used to control the rate at which a resource can publish notifications.
	 * Upon receiving a notification with this flag set ON, the receiver should assume that it will
	 * not receive further notification from it for the time period specified in blockageTime field,
	 * and take appropriate action.
	 */
	@XmlElement(namespace = GenesisIIConstants.GENESISII_NS, name = "publisher-will-be-blocked", nillable = false, required = false)
	private boolean publisherBlockedFromFurtherNotifications;

	@XmlElement(namespace = GenesisIIConstants.GENESISII_NS, name = "blocking-period-in-millis", nillable = false, required = false)
	private Long blockageTime;

	@XmlTransient
	private QName _originalName = null;

	/*
	 * Indicate whether the notification messages are sent to the consumers holding subscription on
	 * the current resource only or consumers holding subscription in another resource can also
	 * receive notification. Such a scenario is desirable to pass ByteIO notifications through the
	 * parent RNS directories containing the publisher ByteIO resource.
	 */
	@XmlTransient
	protected boolean useIndirectPublishers;

	/*
	 * How to retrieve the resource keys of the resources that will be used as indirect publishers.
	 */
	@XmlTransient
	protected String indirectPublishersRetrieveQuery;

	@SuppressWarnings({ "unused", "unchecked" })
	private void afterUnmarshal(Unmarshaller u, Object parent)
	{
		if (parent != null && parent instanceof JAXBElement) {
			JAXBElement<? extends NotificationMessageContents> jaxbe = (JAXBElement<? extends NotificationMessageContents>) parent;
			_originalName = jaxbe.getName();
		}

		Iterator<Element> iter = _any.iterator();
		while (iter.hasNext()) {
			Element e = iter.next();
			QName elementName = new QName(e.getNamespaceURI(), e.getLocalName());
			if (elementName.equals(AdditionalUserDataConstants.ELEMENT_QNAME)) {
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

	@XmlTransient
	public MessageElement[] getAdditionalAttributes()
	{
		return _additionalAttributes;
	}

	public void setAdditionalAttributes(MessageElement[] additionalAttributes)
	{
		this._additionalAttributes = additionalAttributes;
	}

	@XmlTransient
	public boolean isPublisherBlockedFromFurtherNotifications()
	{
		return publisherBlockedFromFurtherNotifications;
	}

	public void setPublisherBlockedFromFurtherNotifications(boolean publisherBlockedFromFurtherNotifications)
	{
		this.publisherBlockedFromFurtherNotifications = publisherBlockedFromFurtherNotifications;
	}

	@XmlTransient
	public Long getBlockageTime()
	{
		return blockageTime;
	}

	public void setBlockageTime(Long blockageTime)
	{
		this.blockageTime = blockageTime;
	}

	@XmlTransient
	public boolean isUseIndirectPublishers()
	{
		return useIndirectPublishers;
	}

	public void setUseIndirectPublishers(boolean useIndirectPublishers)
	{
		this.useIndirectPublishers = useIndirectPublishers;
	}

	@XmlTransient
	public String getIndirectPublishersRetrieveQuery()
	{
		return indirectPublishersRetrieveQuery;
	}

	public void setIndirectPublishersRetrieveQuery(String indirectPublishersRetrieveQuery)
	{
		this.indirectPublishersRetrieveQuery = indirectPublishersRetrieveQuery;
	}

	final public Collection<Element> any()
	{
		return _any;
	}

	final public <Type extends AdditionalUserData> Type additionalUserData(Class<Type> type) throws JAXBException
	{
		if (_additionalUserData == null)
			return null;

		return AdditionalUserData.fromElement(type, _additionalUserData);
	}

	final public void additionalUserData(AdditionalUserData data) throws JAXBException
	{
		_additionalUserData = (data == null) ? null : AdditionalUserData.toMessageElement(data);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	final public MessageElement toAxisType() throws JAXBException
	{
		JAXBContext context = JAXBContext.newInstance(getClass());
		Marshaller m = context.createMarshaller();
		DOMResult result = new DOMResult();
		if (_originalName != null) {
			JAXBElement jaxbe = new JAXBElement(_originalName, getClass(), this);
			m.marshal(jaxbe, result);
		} else
			m.marshal(this, result);
		Node n = result.getNode();
		if (n instanceof Document)
			return new MessageElement(((Document) n).getDocumentElement());
		return new MessageElement((Element) result.getNode());
	}

	@Override
	public Object clone()
	{
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(this);
			oos.close();
			baos.close();
			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bais);
			NotificationMessageContents copy = (NotificationMessageContents) ois.readObject();
			copy.setAdditionalAttributes(cloneAdditionalAttributes());
			return copy;
		} catch (IOException e) {
			throw new RuntimeException("Unable to clone message contents.", e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Unable to clone message contents.", e);
		}
	}

	/*
	 * Only used when the notification message is supposed to indirectly propagated through other
	 * resources. Subclasses using indirect notification should override this method to reflect the
	 * desired behavior.
	 */
	public boolean isIgnoreBlockedIndirectPublisher(long blockingTime)
	{
		return false;
	}

	/*
	 * We have to clone the attributes explicitly as JAXB cannot process arbitrary attributes.
	 * Furthermore, to avoid JAXB serialization problem we have defined _additionalAttributes as
	 * transient. So explicit copying is the only option.
	 */
	private MessageElement[] cloneAdditionalAttributes()
	{
		if (_additionalAttributes == null || _additionalAttributes.length == 0)
			return null;
		int attributesCount = _additionalAttributes.length;
		MessageElement[] clonedAttributes = new MessageElement[attributesCount];
		for (int index = 0; index < attributesCount; index++) {
			MessageElement attribute = _additionalAttributes[index];
			clonedAttributes[index] = attribute;
		}
		return clonedAttributes;
	}
}