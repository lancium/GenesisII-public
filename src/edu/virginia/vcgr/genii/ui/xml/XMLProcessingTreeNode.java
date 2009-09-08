package edu.virginia.vcgr.genii.ui.xml;

import javax.swing.tree.DefaultMutableTreeNode;

class XMLProcessingTreeNode extends DefaultMutableTreeNode
{
	static final long serialVersionUID = 0L;
	
	XMLProcessingTreeNode(String processingText)
	{
		super(processingText, false);
	}
}