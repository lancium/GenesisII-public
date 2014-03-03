package edu.virginia.vcgr.genii.gjt.data.xpath;

import java.util.LinkedList;

import edu.virginia.vcgr.jsdl.sweep.parameters.DocumentNodeSweepParameter;

public class XPathBuilder {
	private XPathAttributeNode _attribute = null;
	private LinkedList<XPathNode> _nodes = new LinkedList<XPathNode>();

	public void push(XPathNode newNode) {
		clearAttribute();
		_nodes.addLast(newNode);
	}

	public void setAttribute(XPathAttributeNode attribute) {
		_attribute = attribute;
	}

	public void clearAttribute() {
		setAttribute(null);
	}

	public XPathNode pop() {
		clearAttribute();
		return _nodes.removeLast();
	}

	public XPathNode peek() {
		return _nodes.getLast();
	}

	public void resetCount() {
		XPathNode top = peek();
		if (!(top instanceof XPathIterableNode))
			throw new IllegalStateException(String.format(
					"Can't reset a XPathNode %s.", top));

		((XPathIterableNode) top).reset();
	}

	public void iterate() {
		XPathNode top = peek();
		if (!(top instanceof XPathIterableNode))
			throw new IllegalStateException(String.format(
					"Can't iterate over XPathNode %s.", top));

		((XPathIterableNode) top).next();
	}

	public DocumentNodeSweepParameter toSubstringParameter(int offset,
			int length) {
		NamespacePrefixMap prefixMap = new NamespacePrefixMap();
		StringBuilder builder = new StringBuilder();

		for (XPathNode node : _nodes)
			builder.append(String.format("/%s", node.toString(prefixMap)));

		if (_attribute != null)
			builder.append(String.format("@%s", _attribute.toString(prefixMap)));

		return new DocumentNodeSweepParameter(String.format(
				"substring(%s, %d, %d)", builder, offset, length),
				prefixMap.getNamespaceBindings());
	}
}