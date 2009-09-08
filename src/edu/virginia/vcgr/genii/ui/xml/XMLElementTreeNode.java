package edu.virginia.vcgr.genii.ui.xml;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

class XMLElementTreeNode extends QNameBasedTreeNode
{
	static final long serialVersionUID = 0L;
	
	XMLElementTreeNode(StartElement element)
	{
		super(element.getName());
		
		Iterator<?> attrs = element.getAttributes();
		while (attrs.hasNext())
		{
			Attribute attr = (Attribute)attrs.next();
			add(new XMLAttributeTreeNode(attr));
		}
	}
	
	@Override
	public String asString(String tabs)
	{
		StringBuilder builder = new StringBuilder();
		
		Collection<XMLTreeNode> attributeNodes = new LinkedList<XMLTreeNode>();
		Collection<XMLTreeNode> elementNodes = new LinkedList<XMLTreeNode>();
		Collection<XMLTreeNode> textNodes = new LinkedList<XMLTreeNode>();
		
		for (int lcv = 0; lcv < getChildCount(); lcv++)
		{
			XMLTreeNode node = (XMLTreeNode)getChildAt(lcv);
			if (node instanceof XMLAttributeTreeNode)
				attributeNodes.add(node);
			else if (node instanceof XMLElementTreeNode)
				elementNodes.add(node);
			else if (node instanceof XMLTextContentTreeNode)
				textNodes.add(node);
		}
		
		builder.append(String.format("%s<%s", tabs, super.asString(tabs)));
		for (XMLTreeNode attrNode : attributeNodes)
		{
			builder.append(String.format("\n%s%s", tabs + "    ",
				attrNode.asString(tabs)));
		}
		
		if (elementNodes.size() == 0 && textNodes.size() == 0)
			builder.append("/>");
		else
		{
			builder.append(">");
			
			if (elementNodes.size() == 0)
			{
				for (XMLTreeNode node : textNodes)
					builder.append(node.asString(tabs));
				builder.append(String.format("</%s>", super.asString(tabs)));
			} else
			{
				builder.append("\n");
				for (XMLTreeNode node : textNodes)
					builder.append(String.format("%s%s\n", tabs + "  ",
						node.asString(tabs)));
				for (XMLTreeNode node : elementNodes)
					builder.append(String.format("%s\n",
						node.asString(tabs + "  ")));
				builder.append(String.format("%s</%s>", tabs, 
					super.asString(tabs)));
			}
		}
		
		return builder.toString();
	}
}