package edu.virginia.vcgr.genii.client.wsrf.wsn.topic;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

public class TopicPath implements Serializable
{
	static final long serialVersionUID = 0L;
	
	static final private Pattern QNAME_PATTERN = Pattern.compile(
		"^\\{([^\\}]+)\\}(.+)$");
	
	private LinkedList<TopicNode> _path;
	
	private void addChild(String child)
	{
		for (String element : child.split("/"))
		{
			if (element == null || (element = element.trim()).length() == 0)
				throw new IllegalArgumentException(String.format(
					"Topic path element \"%s\" is not legal.", child));
			
			_path.addLast(_path.peekLast().deriveChildNode(element));
		}
	}
	
	public TopicPath(QName root)
	{
		_path = new LinkedList<TopicNode>();
		_path.add(new TopicNode(root));
	}
	
	public TopicPath(TopicPath original) 
	{
		if (original == null)
			throw new IllegalArgumentException(
				"Original topic path cannot be null.");
		
		_path = new LinkedList<TopicNode>(original._path);
	}
	
	public TopicPath(TopicPath parent, QName child)
	{
		this(parent);
		_path.addLast(new TopicNode(child));
	}
	
	public TopicPath(TopicPath parent, String child, String...children)
	{
		this(parent);
		
		addChild(child);
		for (String c : children)
			addChild(c);
	}
	
	final public QName root()
	{
		return _path.peekFirst().name();
	}
	
	final public TopicPathExpression expression(NamespaceContext context)
	{
		return new TopicPathExpression(context, _path);
	}
	
	final public TopicPathExpression expression()
	{
		return expression(null);
	}
	
	final public TopicQueryExpression asSimpleQueryExpression()
	{
		return TopicQueryDialects.createSimpleExpression(root());
	}
	
	final public TopicQueryExpression asConcreteQueryExpression()
	{
		return TopicQueryDialects.createConcreteExpression(this);
	}
	
	final public boolean equals(TopicPath other)
	{
		if (_path.size() != other._path.size())
			return false;
		
		Iterator<TopicNode> me = _path.iterator();
		Iterator<TopicNode> you = other._path.iterator();
		
		while (me.hasNext())
		{
			if (!me.next().equals(you.next()))
				return false;
		}
		
		return true;
	}
	
	@Override
	final public boolean equals(Object other)
	{
		if (other instanceof TopicPath)
			return equals((TopicPath)other);
		
		return false;
	}
	
	@Override
	final public int hashCode()
	{
		return _path.hashCode();
	}
	
	@Override
	final public String toString()
	{
		return expression().toString();
	}
	
	final public boolean contains(TopicPath other)
	{
		if (_path.size() > other._path.size())
			return false;
		
		Iterator<TopicNode> me = _path.iterator();
		Iterator<TopicNode> you = other._path.iterator();
		
		while (me.hasNext())
		{
			TopicNode mine = me.next();
			TopicNode yours = you.next();
			
			if (!mine.equals(yours))
				return false;
		}
		
		return true;
	}
	
	final public boolean containedBy(TopicPath other)
	{
		return other.contains(this);
	}
	
	final public LinkedList<QName> pathComponents()
	{
		LinkedList<QName> ret = new LinkedList<QName>();
		
		for (TopicNode node : _path)
			ret.add(node.name());
		
		return ret;
	}
	
	/**
	 * This is a helper function that makes creating topic paths MUCH easier.
	 * 
	 * @param pathElements An arbitrary number of parameters that identify
	 * the topic path.  For the most part, any of these elements can either
	 * be a QName, a String, or a NamespaceContext.  The very first 
	 * non-namespace context element can also be a TopicPath representing a 
	 * parent for the new path.  If an element is a
	 * NamespaceContext, it then becomes the authoritative entity for
	 * describing how to map prefixes to namespace URIs for elements that
	 * follow it.  If an element is a QName, then it represents itself in
	 * the topic path, but also becomes the authoritative namespace for
	 * elements in the path that follow that do not have a namespace or prefix
	 * given.  If an element is a string, then it is a string of the form:
	 * 		[ns-prefix:]topicName[/[ns-prefix:]topicName...].
	 * 
	 * This list of path elements must have at least one QName, or one
	 * string representation that resolves to a valid QName using the rules
	 * defined above.
	 * 
	 * @return The resultant Topic path.
	 */
	static public TopicPath createTopicPath(Object...pathElements)
	{
		TopicPath current = null;
		NamespaceContext currentContext = null;
		String currentNamespaceURI = null;
		String currentPrefix = null;
		
		for (Object element : pathElements)
		{
			if (element instanceof NamespaceContext)
				currentContext = (NamespaceContext)element;
			else if (element instanceof TopicPath)
			{
				if (current != null)
					throw new IllegalArgumentException(
						"Encountered a TopicPath element after the" +
						" root topic was already resolved.");
				
				current = (TopicPath)element;
				currentNamespaceURI = current.root().getNamespaceURI();
				currentPrefix = current.root().getPrefix();
				if (currentPrefix != null && currentPrefix.length() == 0)
					currentPrefix = null;
			} else if (element instanceof QName)
			{
				QName qElement = (QName)element;
				currentNamespaceURI = qElement.getNamespaceURI();
				currentPrefix = qElement.getPrefix();
				if (currentPrefix.length() == 0)
					currentPrefix = null;
				if (current == null)
					current = new TopicPath(qElement);
				else
					current = new TopicPath(current, qElement);
			} else
			{
				String path = element.toString();
				for (String pathElement : path.split("/"))
				{
					if (pathElement == null)
						throw new IllegalArgumentException(String.format(
							"TopicPath element %s is invalid.", path));
					pathElement = pathElement.trim();
					if (pathElement.length() == 0)
						throw new IllegalArgumentException(String.format(
							"TopicPath element %s is invalid.", path));
					
					Matcher matcher = QNAME_PATTERN.matcher(pathElement);
					if (matcher.matches())
					{
						QName qn = QName.valueOf(pathElement);
						current = new TopicPath(current, qn);
						currentPrefix = qn.getPrefix();
						if (currentPrefix != null && currentPrefix.length() == 0)
							currentPrefix = null;
						currentNamespaceURI = qn.getNamespaceURI();
						continue;
					}
					
					int index = pathElement.indexOf(':');
					if (index < 0)
					{
						if (current == null)
							throw new IllegalArgumentException(String.format(
								"TopicPath element %s is invalid " +
								"without preceeding topic definnitions.", path));
						
						current = new TopicPath(current, new QName(
							currentNamespaceURI, pathElement,
							(currentPrefix == null) ? 
								XMLConstants.DEFAULT_NS_PREFIX : currentPrefix));
					} else
					{
						String prefix = pathElement.substring(0, index).trim();
						String localName = pathElement.substring(index + 1).trim();
						
						if (prefix.length() == 0 || localName.length() == 0)
							throw new IllegalArgumentException(String.format(
								"TopicPath element %s is invalid.", path));
						
						if (currentPrefix != null && prefix.equals(currentPrefix))
							current = new TopicPath(current, new QName(
								currentNamespaceURI, localName, prefix));
						else if (currentContext != null)
						{
							String nsURI = currentContext.getNamespaceURI(prefix);
							if (nsURI == null)
								throw new IllegalArgumentException(String.format(
									"TopicPath element %s is invalid.", path));
							current = new TopicPath(current, new QName(
								nsURI, localName, prefix));
							currentNamespaceURI = nsURI;
							currentPrefix = prefix;
						} else
							throw new IllegalArgumentException(String.format(
								"TopicPath element %s is invalid.", path));
					}
				}
			}
		}
		
		if (current == null)
			throw new IllegalArgumentException(
				"No topic defined!");
		
		return current;
	}
	
	static public void main(String []args)
	{
		TopicPath path = new TopicPath(new QName(
			"http://tempuri.org", "one", "ts1"));
		System.err.println(TopicPath.createTopicPath(
			path, "two"));
	}
}