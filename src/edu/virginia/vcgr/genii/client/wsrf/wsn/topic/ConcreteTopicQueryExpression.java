package edu.virginia.vcgr.genii.client.wsrf.wsn.topic;

import javax.xml.namespace.QName;

class ConcreteTopicQueryExpression extends AbstractTopicQueryExpression
{
	static final long serialVersionUID = 0L;
	
	private TopicPath _path;
	
	@Override
	protected String toString(NamespaceFactory prefixFactory)
	{
		String lastNamespaceURI = null;
		String lastPrefix = null;
		StringBuilder builder = new StringBuilder();
		
		for (QName e : _path.pathComponents())
		{
			if (builder.length() != 0)
				builder.append('/');
			if (lastNamespaceURI == null ||
				!lastNamespaceURI.equals(e.getNamespaceURI()))
			{
				lastPrefix = prefixFactory.getPrefix(e.getNamespaceURI());
				builder.append(lastPrefix + ":");
			}
			builder.append(e.getLocalPart());
			lastNamespaceURI = e.getNamespaceURI();
		}
		
		return builder.toString();
	}
	
	ConcreteTopicQueryExpression(TopicPath path)
	{
		super(TopicQueryDialects.Concrete);
		
		_path = path;
	}

	@Override
	final public boolean matches(TopicPath topic)
	{
		return _path.contains(topic);
	}

	@Override
	final public TopicPath toTopicPath()
	{
		return _path;
	}
}