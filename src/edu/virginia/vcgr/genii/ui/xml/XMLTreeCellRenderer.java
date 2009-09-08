package edu.virginia.vcgr.genii.ui.xml;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

public class XMLTreeCellRenderer extends DefaultTreeCellRenderer
{
	static final long serialVersionUID = 0L;

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus)
	{
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, 
			row, hasFocus);
		
		Font myFont = getFont();
		if (value instanceof XMLAttributeTreeNode)
		{
			setFont(myFont.deriveFont(Font.ITALIC, myFont.getSize() + 2.0f));
			setIcon(XMLTreeIcons.attributeIcon());
			setForeground(Color.GREEN.darker().darker());
		} else if (value instanceof XMLElementTreeNode)
		{
			setFont(myFont.deriveFont(Font.BOLD));
			setIcon(XMLTreeIcons.elementIcon());
			setForeground(Color.BLUE.darker());
		} else if (value instanceof XMLErrorTreeNode)
		{
			setIcon(XMLTreeIcons.errorIcon());
			setForeground(Color.RED.darker());
		} else if (value instanceof XMLProcessingTreeNode)
		{
			setIcon(null);
			setForeground(Color.GRAY.darker());
			setFont(myFont.deriveFont(Font.ITALIC));
		} else if (value instanceof XMLTextContentTreeNode)
		{
			setIcon(null);
		} else if (value instanceof XMLTreeNodeDocumentRoot)
		{
			setIcon(null);
			setFont(myFont.deriveFont(Font.BOLD, myFont.getSize() + 4.0f));
		} else
		{
			setIcon(null);
		}
		
		return this;
	}
}