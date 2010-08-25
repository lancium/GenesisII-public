package edu.virginia.vcgr.genii.container.cservices.wsn;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.io.StreamCorruptedException;

import javax.xml.bind.JAXBException;

import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.wsrf.wsn.AdditionalUserData;
import edu.virginia.vcgr.genii.client.wsrf.wsn.NotificationMessageContents;
import edu.virginia.vcgr.genii.client.wsrf.wsn.topic.TopicPath;

public class NotificationMessageOutcallContent
	implements Serializable
{
	static final long serialVersionUID = 0L;
	
	private EndpointReferenceType _subscriptionReference;
	private TopicPath _topic;
	private EndpointReferenceType _publisher;
	private NotificationMessageContents _contents;
	
	private void writeObject(ObjectOutputStream out)
    	throws IOException
	{
		EPRUtils.serializeEPR(out, _subscriptionReference);
		EPRUtils.serializeEPR(out, _publisher);
		out.writeObject(_topic);
		out.writeObject(_contents);
	}
	
	private void readObject(ObjectInputStream in)
		throws IOException, ClassNotFoundException
	{
		_subscriptionReference = EPRUtils.deserializeEPR(in);
		_publisher = EPRUtils.deserializeEPR(in);
		_topic = (TopicPath)in.readObject();
		_contents = (NotificationMessageContents)in.readObject();
	}
	
	@SuppressWarnings("unused")
	private void readObjectNoData() 
    	throws ObjectStreamException
	{
		throw new StreamCorruptedException();
	}
	
	public NotificationMessageOutcallContent(
		EndpointReferenceType subscriptionReference,
		TopicPath topic, EndpointReferenceType publisher,
		NotificationMessageContents contents,
		AdditionalUserData additionalUserData) throws JAXBException
	{
		_subscriptionReference = subscriptionReference;
		_topic = topic;
		_publisher = publisher;
		_contents = (NotificationMessageContents)contents.clone();
		_contents.additionalUserData(additionalUserData);
	}
	
	final public EndpointReferenceType subscriptionReference()
	{
		return _subscriptionReference;
	}
	
	final public TopicPath topic()
	{
		return _topic;
	}
	
	final public EndpointReferenceType publisher()
	{
		return _publisher;
	}
	
	final public NotificationMessageContents contents()
	{
		return _contents;
	}
}
