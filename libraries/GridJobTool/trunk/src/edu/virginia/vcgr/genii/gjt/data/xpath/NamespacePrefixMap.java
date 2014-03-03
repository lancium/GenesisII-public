package edu.virginia.vcgr.genii.gjt.data.xpath;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import edu.virginia.vcgr.jsdl.sweep.parameters.NamespaceBinding;

public class NamespacePrefixMap
{
	private Map<String, String> _map = new HashMap<String, String>();
	private int _next = 1;

	public String prefixForNamespace(String namespaceURI)
	{
		String result = _map.get(namespaceURI);
		if (result == null)
			_map.put(namespaceURI, result = String.format("ns%d", _next++));

		return result;
	}

	public List<NamespaceBinding> getNamespaceBindings()
	{
		List<NamespaceBinding> ret = new Vector<NamespaceBinding>(_map.size());

		for (Map.Entry<String, String> entry : _map.entrySet()) {
			ret.add(new NamespaceBinding(entry.getKey(), entry.getValue()));
		}

		return ret;
	}
}