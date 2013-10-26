package edu.virginia.vcgr.appmgr.patch.builder.tree.menu;

import java.awt.event.ActionEvent;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import edu.virginia.vcgr.appmgr.patch.builder.tree.PatchTree;

class RemovePatchOperationAction extends AbstractPatchMenuAction
{
	static final long serialVersionUID = 0l;

	static private final String NAME = "Remove Patch Operation";

	@Override
	protected boolean shouldBeEnabled(PatchTree tree)
	{
		TreePath[] paths = tree.getSelectionPaths();
		if (paths != null && paths.length > 0) {
			for (TreePath path : paths) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
				if (node.getLevel() != 2)
					return false;
			}

			return true;
		}

		return false;
	}

	RemovePatchOperationAction(PatchTree tree)
	{
		super(NAME, tree);
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		DefaultTreeModel model = (DefaultTreeModel) _tree.getModel();
		TreePath[] paths = _tree.getSelectionPaths();
		for (TreePath path : paths) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
			model.removeNodeFromParent(node);
		}
	}
}