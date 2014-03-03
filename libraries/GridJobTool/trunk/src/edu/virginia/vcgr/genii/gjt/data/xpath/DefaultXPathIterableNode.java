package edu.virginia.vcgr.genii.gjt.data.xpath;

public class DefaultXPathIterableNode extends DefaultXPathNode implements XPathIterableNode
{
	private int _index = 1;

	public DefaultXPathIterableNode(String namespaceURI, String name)
	{
		super(namespaceURI, name);
	}

	@Override
	public void reset()
	{
		_index = 1;
	}

	@Override
	public void next()
	{
		_index++;
	}

	@Override
	public String toString(NamespacePrefixMap prefixMap)
	{
		return String.format("%s[%d]", super.toString(prefixMap), _index);
	}
}