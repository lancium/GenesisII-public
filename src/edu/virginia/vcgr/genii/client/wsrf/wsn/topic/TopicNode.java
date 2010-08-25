package edu.virginia.vcgr.genii.client.wsrf.wsn.topic;

import java.io.Serializable;

import javax.xml.namespace.QName;

public class TopicNode implements Serializable
{
	static final long serialVersionUID = 0L;
	
	private QName _nodeName;
	
	public TopicNode(QName nodeName)
	{
		if (nodeName == null)
			throw new IllegalArgumentException(
				"TopicNode name cannot be null.");
		
		_nodeName = nodeName;
	}
	
	final public TopicNode deriveChildNode(String childName)
	{
		return new TopicNode(new QName(
			_nodeName.getNamespaceURI(), childName));
	}
	
	final public QName name()
	{
		return _nodeName;
	}
	
	final public boolean equals(TopicNode other)
	{
		return _nodeName.equals(other._nodeName);
	}
	
	final public boolean equals(QName other)
	{
		return _nodeName.equals(other);
	}
	
	@Override
	final public boolean equals(Object other)
	{
		if (other instanceof TopicNode)
			return equals((TopicNode)other);
		else if (other instanceof QName)
			return equals((QName)other);
		else
			return false;
	}
	
	@Override
	final public String toString()
	{
		return _nodeName.toString();
	}
	
	@Override
	final public int hashCode()
	{
		return _nodeName.getNamespaceURI().hashCode() ^
			_nodeName.getLocalPart().hashCode();
	}
}