package edu.virginia.vcgr.genii.ui.plugins.queue.history;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import edu.virginia.vcgr.genii.client.history.ExceptionIcon;
import edu.virginia.vcgr.genii.client.history.HistoryEvent;

public class HistoryEventTreeCellRenderer extends DefaultTreeCellRenderer
{
	static final long serialVersionUID = 0L;

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
		int row, boolean hasFocus)
	{
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

		HistoryEventTreeNode node = (HistoryEventTreeNode) value;
		HistoryEvent event = node.event();

		if (event == null) {
			setText("This is a test");
			setIcon(null);
			return this;
		}

		setText(event.title());

		Icon icon = event.eventCategory().information().categoryIcon();
		if (event.eventData().eventException() != null)
			icon = new ExceptionIcon(icon);

		icon = new CompositeIcon(2, LevelIcon.iconForLevel(node.branchLevel()), icon);
		setIcon(icon);

		return this;
	}
}