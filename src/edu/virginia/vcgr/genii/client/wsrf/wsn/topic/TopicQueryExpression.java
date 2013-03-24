package edu.virginia.vcgr.genii.client.wsrf.wsn.topic;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;

import org.apache.axis.message.MessageElement;

public interface TopicQueryExpression
{
	public TopicQueryDialects dialect();

	public boolean matches(TopicPath topic);

	public MessageElement toTopicExpressionElement(QName elementName, String namespacePrefix) throws SOAPException;

	public TopicPath toTopicPath();
}