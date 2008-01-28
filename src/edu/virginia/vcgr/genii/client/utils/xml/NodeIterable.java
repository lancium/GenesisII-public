package edu.virginia.vcgr.genii.client.utils.xml;

import java.util.Iterator;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class NodeIterable implements Iterable<Element>
{
	private Node _parent;
	
	public NodeIterable(Node parent)
	{
		_parent = parent;
	}
	
	@Override
	public Iterator<Element> iterator()
	{
		return new NodeIterator();
	}
	
	private class NodeIterator implements Iterator<Element>
	{
		private int _index;
		private NodeList _children;
		
		public NodeIterator()
		{
			_children = _parent.getChildNodes();
			_index = -1;
		}
		
		@Override
		public boolean hasNext()
		{
			for (int lcv = _index + 1; lcv < _children.getLength(); lcv++)
			{
				Node n = _children.item(lcv);
				if (n.getNodeType() == Node.ELEMENT_NODE)
					return true;
			}
			
			return false;
		}

		@Override
		public Element next()
		{
			for (int lcv = _index + 1; lcv < _children.getLength(); lcv++)
			{
				Node n = _children.item(lcv);
				if (n.getNodeType() == Node.ELEMENT_NODE)
				{
					_index = lcv;
					return (Element)n;
				}
			}
			
			_index = _children.getLength();
			return null;
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException(
				"The remove operation is not supported.");
		}
	}
}