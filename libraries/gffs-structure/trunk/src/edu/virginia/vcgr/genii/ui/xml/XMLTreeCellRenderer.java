package edu.virginia.vcgr.genii.ui.xml;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

public class XMLTreeCellRenderer extends DefaultTreeCellRenderer
{
	static final long serialVersionUID = 0L;

	private Font _originalFont = null;

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
		int row, boolean hasFocus)
	{
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

		synchronized (this) {
			if (_originalFont == null)
				_originalFont = getFont();
		}

		if (value instanceof XMLAttributeTreeNode) {
			setFont(_originalFont.deriveFont(Font.ITALIC, _originalFont.getSize() + 2.0f));
			setIcon(XMLTreeIcons.attributeIcon());
			setForeground(Color.GREEN.darker().darker());
		} else if (value instanceof XMLElementTreeNode) {
			setFont(_originalFont.deriveFont(Font.BOLD));
			setIcon(XMLTreeIcons.elementIcon());
			setForeground(Color.BLUE.darker());
		} else if (value instanceof XMLErrorTreeNode) {
			setFont(_originalFont);
			setIcon(XMLTreeIcons.errorIcon());
			setForeground(Color.RED.darker());
		} else if (value instanceof XMLProcessingTreeNode) {
			setIcon(null);
			setForeground(Color.GRAY.darker());
			setFont(_originalFont.deriveFont(Font.ITALIC));
		} else if (value instanceof XMLTextContentTreeNode) {
			setFont(_originalFont);
			setIcon(null);
		} else if (value instanceof XMLTreeNodeDocumentRoot) {
			setIcon(null);
			setFont(_originalFont.deriveFont(Font.BOLD, _originalFont.getSize() + 4.0f));
		} else {
			setFont(_originalFont);
			setIcon(null);
		}

		return this;
	}
}