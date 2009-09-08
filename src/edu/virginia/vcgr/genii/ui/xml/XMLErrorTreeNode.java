package edu.virginia.vcgr.genii.ui.xml;

import javax.swing.tree.DefaultMutableTreeNode;

class XMLErrorTreeNode extends DefaultMutableTreeNode
{
	static final long serialVersionUID = 0L;
	
	XMLErrorTreeNode(String errorMessage)
	{
		super(errorMessage, false);
	}
}