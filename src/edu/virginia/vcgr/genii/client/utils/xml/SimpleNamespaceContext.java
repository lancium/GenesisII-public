package edu.virginia.vcgr.genii.client.utils.xml;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

public class SimpleNamespaceContext 
	implements NamespaceContext, PrefixResolver, NamespaceResolver
{
	private NamespaceContext _parent = null;
	
	private String _defaultNS = null;
	private Map<String, String> _ns2pre = new HashMap<String, String>();
	private Map<String, String> _pre2ns = new HashMap<String, String>(); 
	
	public SimpleNamespaceContext(NamespaceContext parent)
	{
		_parent = parent;
	}
	
	public SimpleNamespaceContext()
	{
		this(null);
	}
	
	final public void setDefaultNS(String defaultNS)
	{
		if (defaultNS == null)
			throw new IllegalArgumentException(
				"defaultNS cannot be null.");
		
		_defaultNS = defaultNS;
	}
	
	final public void associate(String prefix, String namespace)
	{
		if (prefix == null)
			throw new IllegalArgumentException("Prefix cannot be null.");
		
		if (namespace == null)
			throw new IllegalArgumentException("Namespace cannot be null.");
		
		if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX))
		{
			setDefaultNS(namespace);
			return;
		}
		
		if (prefix.equals(XMLConstants.XML_NS_PREFIX))
			throw new IllegalArgumentException(
				"Prefix cannot be the XML NS prefix.");
		
		if (prefix.equals(XMLConstants.XMLNS_ATTRIBUTE))
			throw new IllegalArgumentException(
				"Prefix cannot be the XMLNS attribute prefix.");
		
		_ns2pre.put(namespace, prefix);
		_pre2ns.put(prefix, namespace);
	}
	
	@Override
	final public String getNamespaceURI(String prefix)
	{
		if (prefix == null)
			throw new IllegalArgumentException(
				"Prefix cannot be null.");
		
		if (prefix.equals(XMLConstants.DEFAULT_NS_PREFIX))
		{
			if (_defaultNS != null)
				return _defaultNS;
			
			if (_parent != null)
				return _parent.getNamespaceURI(prefix);
			
			return XMLConstants.NULL_NS_URI;
		} else if (prefix.equals(XMLConstants.XML_NS_PREFIX))
			return XMLConstants.XML_NS_URI;
		else if (prefix.equals(XMLConstants.XMLNS_ATTRIBUTE))
			return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
		
		String ns = _pre2ns.get(prefix);
		if (ns == null)
		{
			if (_parent != null)
				return _parent.getNamespaceURI(prefix);
			else
				return XMLConstants.NULL_NS_URI;
		}
		
		return ns;
	}

	@Override
	final public String getPrefix(String namespaceURI)
	{
		if (namespaceURI == null)
			throw new IllegalArgumentException("namespaceURI cannot be null.");
		
		if (namespaceURI.equals(XMLConstants.XML_NS_URI))
			return XMLConstants.XML_NS_PREFIX;
		else if (namespaceURI.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI))
			return XMLConstants.XMLNS_ATTRIBUTE;
		else
		{
			if (_defaultNS != null && _defaultNS.equals(namespaceURI))
				return XMLConstants.DEFAULT_NS_PREFIX;
			
			String prefix = _ns2pre.get(namespaceURI);
			if (prefix == null && _parent != null)
				prefix = _parent.getPrefix(namespaceURI);
			
			return prefix;
		}
	}

	@Override
	final public Iterator<String> getPrefixes(String namespaceURI)
	{
		if (namespaceURI == null)
			throw new IllegalArgumentException(
				"NamespaceURI cannot be null.");
		
		LinkedList<String> prefixes = new LinkedList<String>();
		
		if (namespaceURI.equals(XMLConstants.XML_NS_URI))
			prefixes.add(XMLConstants.XML_NS_PREFIX);
		else if (namespaceURI.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI))
			prefixes.add(XMLConstants.XMLNS_ATTRIBUTE);
		else
		{
			String prefix;
			if (_defaultNS != null && _defaultNS.equals(namespaceURI))
				prefixes.add(XMLConstants.DEFAULT_NS_PREFIX);
		
			prefix = _ns2pre.get(namespaceURI);
			if (prefix != null)
				prefixes.add(prefix);
			
			if (_parent != null)
			{
				Iterator<?> parents = _parent.getPrefixes(namespaceURI);
				while (parents.hasNext())
					prefixes.add(parents.next().toString());
			}
		}
		
		return prefixes.iterator();
	}
	
	final public SimpleNamespaceContext deriveNewContext()
	{
		return new SimpleNamespaceContext(this);
	}
}