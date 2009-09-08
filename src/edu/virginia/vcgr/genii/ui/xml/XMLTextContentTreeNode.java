package edu.virginia.vcgr.genii.ui.xml;

import javax.swing.tree.DefaultMutableTreeNode;

class XMLTextContentTreeNode extends DefaultMutableTreeNode
	implements XMLTreeNode
{
	static final long serialVersionUID = 0L;
	
	XMLTextContentTreeNode(String text)
	{
		super(text, false);
	}

	@Override
	public String asString(String tabs)
	{
		return getUserObject().toString();
	}
}