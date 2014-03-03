package edu.virginia.vcgr.appmgr.util;

import java.util.Iterator;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ElementIterable implements Iterable<Element>
{
	private NodeList _children;

	public ElementIterable(NodeList children)
	{
		_children = children;
	}

	@Override
	public Iterator<Element> iterator()
	{
		return new ElementIterator();
	}

	private class ElementIterator implements Iterator<Element>
	{
		private int _next;

		private int findNextElement(int start)
		{
			for (int lcv = start; lcv < _children.getLength(); lcv++) {
				if (_children.item(lcv).getNodeType() == Node.ELEMENT_NODE)
					return lcv;
			}

			return _children.getLength();
		}

		private ElementIterator()
		{
			_next = findNextElement(0);
		}

		@Override
		public boolean hasNext()
		{
			return _next < _children.getLength();
		}

		@Override
		public Element next()
		{
			if (_next >= _children.getLength())
				return null;

			Element ret = (Element) _children.item(_next);
			_next = findNextElement(_next + 1);
			return ret;
		}

		@Override
		public void remove()
		{
			throw new RuntimeException("Not allowed to delete elements using the element iterator.");
		}
	}
}