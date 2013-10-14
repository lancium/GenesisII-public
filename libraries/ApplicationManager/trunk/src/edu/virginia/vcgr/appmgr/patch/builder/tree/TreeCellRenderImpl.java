package edu.virginia.vcgr.appmgr.patch.builder.tree;

import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import edu.virginia.vcgr.appmgr.patch.PatchOperationType;
import edu.virginia.vcgr.appmgr.patch.builder.PatchRC;

public class TreeCellRenderImpl extends DefaultTreeCellRenderer
{
	static private final BuilderIcon WARNING_ICON = new BuilderIcon("warning.gif");
	static private final BuilderIcon OK_ICON = new BuilderIcon("ok.gif");

	static final long serialVersionUID = 0L;

	private PatchRC _rc;

	TreeCellRenderImpl(PatchRC rc)
	{
		_rc = rc;
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
		int row, boolean hasFocus)
	{
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

		if (value instanceof DefaultMutableTreeNode) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
			if (node.getLevel() == 2) {
				PatchOperationType pot = null;

				Object userObj = node.getUserObject();
				if (userObj != null && userObj instanceof AtomBundle)
					pot = ((AtomBundle) userObj).getAtom().getOperationType();

				String path = value.toString();
				if (_rc.cautions().contains(path))
					setIcon(WARNING_ICON);
				else
					setIcon(OK_ICON);

				if (pot != null)
					setText(String.format("[%s] %s", pot, node));
			}
		}

		return this;
	}
}