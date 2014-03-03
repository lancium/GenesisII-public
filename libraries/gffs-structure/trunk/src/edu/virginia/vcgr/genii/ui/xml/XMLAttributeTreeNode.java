package edu.virginia.vcgr.genii.ui.xml;

import javax.xml.stream.events.Attribute;

class XMLAttributeTreeNode extends QNameBasedTreeNode
{
	static final long serialVersionUID = 0L;

	XMLAttributeTreeNode(Attribute attribute)
	{
		super(attribute.getName());

		add(new XMLTextContentTreeNode(attribute.getValue()));
	}

	@Override
	public String asString(String tabs)
	{
		return String.format("%s=\"%s\"", super.asString(tabs), ((XMLTreeNode) getFirstChild()).asString(tabs));
	}
}