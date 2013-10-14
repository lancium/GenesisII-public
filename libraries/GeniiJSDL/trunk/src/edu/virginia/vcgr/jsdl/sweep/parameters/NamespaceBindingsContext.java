/*
 * This code was developed by Mark Morgan (mmm2a@virginia.edu) at the University of Virginia and is
 * an implementation of JSDL, JSDL ParameterSweep and other JSDL related specifications from the
 * OGF.
 * 
 * Copyright 2010 University of Virginia
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.virginia.vcgr.jsdl.sweep.parameters;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.xml.namespace.NamespaceContext;

/**
 * @author Mark Morgan (mmm2a@virginia.edu)
 */
public class NamespaceBindingsContext implements NamespaceContext
{
	private Map<String, Collection<String>> _uri2PrefixMap;
	private Map<String, String> _prefix2UriMap;

	public NamespaceBindingsContext(Collection<NamespaceBinding> bindings)
	{
		_uri2PrefixMap = new HashMap<String, Collection<String>>(bindings.size());
		_prefix2UriMap = new HashMap<String, String>(bindings.size());

		for (NamespaceBinding binding : bindings) {
			_prefix2UriMap.put(binding.prefix(), binding.namespaceURI());
			Collection<String> list = _uri2PrefixMap.get(binding.namespaceURI());
			if (list == null)
				_uri2PrefixMap.put(binding.namespaceURI(), list = new Vector<String>());
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
