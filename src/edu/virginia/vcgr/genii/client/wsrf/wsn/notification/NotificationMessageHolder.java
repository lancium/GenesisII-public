package edu.virginia.vcgr.genii.client.wsrf.wsn.notification;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.apache.axis.message.MessageElement;
import org.oasis_open.wsn.base.InvalidFilterFaultType;
import org.oasis_open.wsn.base.NotificationMessageHolderType;
import org.oasis_open.wsn.base.NotificationMessageHolderTypeMessage;
import org.oasis_open.wsn.base.TopicNotSupportedFaultType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.wsrf.WSRFConstants;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMessageContents;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicQueryDialects;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicQueryExpression;

public class NotificationMessageHolder
{
	static final QName TOPIC_QNAME = new QName(
		WSRFConstants.WSN_TOPIC_NS, "Topic");
	
	private EndpointReferenceType _subscriptionReference;
	private EndpointReferenceType _publisherReference;
	private NotificationMessageContents _contents = null;
	private TopicPath _topic;
	
	public NotificationMessageHolder(EndpointReferenceType subscriptionReference,
		EndpointReferenceType publisherReference, TopicPath topic,
		NotificationMessageContents contents)
	{
		_subscriptionReference = subscriptionReference;
		_publisherReference = publisherReference;
		_contents = contents;
		_topic = topic;
	}
	
	public NotificationMessageHolder(
		Class<? extends NotificationMessageContents> contentsType,
		NotificationMessageHolderType axisHolder) throws JAXBException,
			InvalidFilterFaultType, TopicNotSupportedFaultType
	{
		_subscriptionReference = axisHolder.getSubscriptionReference();
		_publisherReference = axisHolder.getProducerReference();
		
		NotificationMessageHolderTypeMessage message = axisHolder.getMessage();
		if (message != null)
		{
			MessageElement []any = message.get_any();
			if (any != null)
			{
				if (any.length > 0)
				{
					if (any.length > 1)
						throw new IllegalArgumentException(String.format(
							"Expected a message contents of size 0 or 1, but got %d.",
							any.length));
					
					if (contentsType != null)
					{
						JAXBContext context = JAXBContext.newInstance(
							contentsType);
						Unmarshaller u = context.createUnmarshaller();
						_contents = u.unmarshal(any[0], contentsType).getValue();
					}
				}
			}
		}
		
		MessageElement []any = axisHolder.get_any();
		if (any != null)
		{
			for (MessageElement value : any)
			{
				QName name = value.getQName();
				if (name.equals(TOPIC_QNAME))
				{
					TopicQueryExpression query = 
						TopicQueryDialects.createFromElement(value);
					_topic = query.toTopicPath();
				}
			}
		}
	}
	
	final public EndpointReferenceType subscriptionReference()
	{
		return _subscriptionReference;
	}
	
	final public EndpointReferenceType publisherReference()
	{
		return _publisherReference;
	}
	
	final public TopicPath topic()
	{
		return _topic;
	}
	
	final public <Type extends NotificationMessageContents> Type contents(
		Class<Type> contentsType)
	{
		return contentsType.cast(_contents);
	}
	
	final public NotificationMessageHolderType toAxisType()
		throws JAXBException, SOAPException
	{
		MessageElement me = null;
		
		if (_contents != null)
			me = _contents.toAxisType();
		
		return new NotificationMessageHolderType(
			_subscriptionReference, _publisherReference,
			me == null ? null :
				new NotificationMessageHolderTypeMessage(
					new MessageElement[] { me } ),
			_topic == null ? null : new MessageElement[] {
				_topic.asConcreteQueryExpression().toTopicExpressionElement(
					TOPIC_QNAME, "ts%d")
			});
	}
}