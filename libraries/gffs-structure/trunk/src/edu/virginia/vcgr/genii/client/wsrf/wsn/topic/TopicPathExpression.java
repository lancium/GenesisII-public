package edu.virginia.vcgr.genii.client.wsrf.wsn.topic;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

import edu.virginia.vcgr.genii.client.utils.xml.NamespaceResolver;
import edu.virginia.vcgr.genii.client.utils.xml.SimpleNamespaceContext;

public class TopicPathExpression
{
	private SimpleNamespaceContext _nsContext;
	private TopicNode[] _nodes;

	private QName getQName(TopicNode node)
	{
		QName orig = node.name();
		String prefix = _nsContext.getPrefix(orig.getNamespaceURI());
		return new QName(orig.getNamespaceURI(), orig.getLocalPart(), prefix);
	}

	TopicPathExpression(NamespaceContext nsContext, Collection<TopicNode> nodes)
	{
		_nsContext = new SimpleNamespaceContext(nsContext);
		_nodes = new TopicNode[nodes.size()];
		nodes.toArray(_nodes);

		int preCount = 1;
		for (TopicNode node : _nodes) {
			QName name = node.name();
			String prefix = _nsContext.getPrefix(name.getNamespaceURI());
			if (prefix == null) {
				prefix = name.getPrefix();
				if (prefix == null || prefix.length() == 0)
					prefix = String.format("ns%d", preCount++);
				_nsContext.associate(prefix, name.getNamespaceURI());
			}
		}
	}

	final public NamespaceContext context()
	{
		return _nsContext;
	}

	@Override
	final public String toString()
	{
		String lastNamespaceURI = null;
		StringBuilder builder = new StringBuilder();

		for (TopicNode node : _nodes) {
			QName name = node.name();
			if (builder.length() != 0)
				builder.append('/');
			if ((lastNamespaceURI == null) || !lastNamespaceURI.equals(name.getNamespaceURI())) {
				lastNamespaceURI = name.getNamespaceURI();
				String prefix = _nsContext.getPrefix(lastNamespaceURI);
				builder.append(String.format("%s:%s", prefix, name.getLocalPart()));
			} else
				builder.append(name.getLocalPart());
		}

		return builder.toString();
	}

	final public TopicPath topicPath()
	{
		TopicPath path = new TopicPath(getQName(_nodes[0]));
		for (int lcv = 1; lcv < _nodes.length; lcv++)
			path = new TopicPath(path, getQName(_nodes[lcv]));

		return path;
	}

	static private class NamespaceMapResolver implements NamespaceResolver
	{
		private Map<String, String> _map;

		private NamespaceMapResolver(Map<String, String> map)
		{
			if (map == null)
				throw new IllegalArgumentException("Map cannot be null.");

			_map = map;
		}

		@Override
		final public String getNamespaceURI(String prefix)
		{
			return _map.get(prefix);
		}
	}

	static public TopicPathExpression fromString(NamespaceContext nsContext, String expression)
	{
		if (!(nsContext instanceof NamespaceResolver))
			nsContext = new SimpleNamespaceContext(nsContext);

		return fromString((NamespaceResolver) nsContext, expression);
	}

	static public TopicPathExpression fromString(Map<String, String> prefix2nsMap, String expression)
	{
		return fromString(new NamespaceMapResolver(prefix2nsMap), expression);
	}

	static public TopicPathExpression fromString(NamespaceResolver resolver, String expression)
	{
		String lastNamespaceURI = null;
		String lastPrefix = null;
		LinkedList<TopicNode> nodes = new LinkedList<TopicNode>();

		for (String element : expression.split("/")) {
			if (element == null || element.length() == 0)
				throw new IllegalArgumentException(String.format("\"%s\" is not a valid Topic expression.", expression));

			int index = element.indexOf(':');
			if (index < 0) {
				if (lastNamespaceURI == null)
					throw new IllegalArgumentException(String.format("Can't tell what namespace to start with."));

				nodes.addLast(new TopicNode(new QName(lastNamespaceURI, element, lastPrefix)));
			} else if (index > 0) {
				lastPrefix = element.substring(0, index);
				element = element.substring(index + 1);
				lastNamespaceURI = resolver.getNamespaceURI(lastPrefix);
				if (lastNamespaceURI == null)
					throw new IllegalArgumentException(String.format("Can't find a namespace for prefix %s.", lastPrefix));

				nodes.addLast(new TopicNode(new QName(lastNamespaceURI, element, lastPrefix)));
			} else
				throw new IllegalArgumentException(String.format("%s is not a valid topic node.", element));
		}

		return new TopicPathExpression(null, nodes);
	}

	static public void main(String[] args)
	{
		String NS1 = "http://vcgr.cs.virginia.edu/1";
		String NS2 = "http://vcgr.cs.virginia.edu/2";
		String NS3 = "http://vcgr.cs.virginia.edu/3";

		TopicPath path = new TopicPath(new QName(NS1, "t1", "top1"));
		path = new TopicPath(path, "t2");
		path = new TopicPath(path, new QName(NS1, "t3", "topother"));
		path = new TopicPath(path, new QName(NS2, "t4", "top2"));
		path = new TopicPath(path, new QName(NS3, "t5"));

		System.out.println(path.expression());
	}
}