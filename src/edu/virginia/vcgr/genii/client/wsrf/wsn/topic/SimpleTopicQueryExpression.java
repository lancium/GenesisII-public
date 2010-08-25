package edu.virginia.vcgr.genii.client.wsrf.wsn.topic;

import javax.xml.namespace.QName;

class SimpleTopicQueryExpression extends AbstractTopicQueryExpression
{
	static final long serialVersionUID = 0L;
	
	private QName _root;

	@Override
	protected String toString(NamespaceFactory prefixFactory)
	{
		String prefix = prefixFactory.getPrefix(_root.getNamespaceURI());
		return String.format("%s:%s", prefix, _root.getLocalPart());
	}
	
	SimpleTopicQueryExpression(QName root)
	{
		super(TopicQueryDialects.Simple);
		
		_root = root;
	}
	
	@Override
	final public boolean matches(TopicPath topic)
	{
		return topic.root().equals(_root);
	}

	@Override
	final public TopicPath toTopicPath()
	{
		return new TopicPath(_root);
	}
}