package edu.virginia.vcgr.genii.client.jsdl.sweep.parameters;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.xml.namespace.NamespaceContext;

public class NamespaceBindingsContext implements NamespaceContext
{
	private Map<String, Collection<String>> _uri2PrefixMap;
	private Map<String, String> _prefix2UriMap;
	
	public NamespaceBindingsContext(Collection<NamespaceBinding> bindings)
	{
		_uri2PrefixMap = new HashMap<String, Collection<String>>(
			bindings.size());
		_prefix2UriMap  = new HashMap<String, String>(bindings.size());
		
		for (NamespaceBinding binding : bindings)
		{
			_prefix2UriMap.put(binding.prefix(), binding.namespaceURI());
			Collection<String> list = _uri2PrefixMap.get(
				binding.namespaceURI());
			if (list == null)
				_uri2PrefixMap.put(binding.namespaceURI(),
					list = new Vector<String>());
			list.add(binding.prefix());
		}
	}
	
	@Override
	final public String getNamespaceURI(String prefix)
	{
		return _prefix2UriMap.get(prefix);
	}

	@Override
	final public Iterator<String> getPrefixes(String namespaceURI)
	{
		Collection<String> ret = _uri2PrefixMap.get(namespaceURI);
		if (ret == null)
			return null;
		
		return ret.iterator();
	}
	
	@Override
	final public String getPrefix(String namespaceURI)
	{
		Iterator<String> iter = getPrefixes(namespaceURI);
		if (iter == null)
			return null;
		
		return iter.next();
	}
}