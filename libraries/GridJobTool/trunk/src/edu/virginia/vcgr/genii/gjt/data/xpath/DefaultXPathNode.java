package edu.virginia.vcgr.genii.gjt.data.xpath;

public class DefaultXPathNode implements XPathNode
{
	protected String _namespaceURI;
	protected String _name;

	public DefaultXPathNode(String namespaceURI, String name)
	{
		_namespaceURI = namespaceURI;
		_name = name;
	}

	@Override
	public String toString(NamespacePrefixMap prefixMap)
	{
		return String.format("%s:%s", prefixMap.prefixForNamespace(_namespaceURI), _name);
	}
}