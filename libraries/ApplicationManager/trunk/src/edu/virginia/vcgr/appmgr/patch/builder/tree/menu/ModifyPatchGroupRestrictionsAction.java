package edu.virginia.vcgr.appmgr.patch.builder.tree.menu;

import java.awt.Dialog.ModalityType;
import java.awt.event.ActionEvent;

import javax.swing.JFrame;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import edu.virginia.vcgr.appmgr.patch.PatchRestrictions;
import edu.virginia.vcgr.appmgr.patch.builder.tree.PatchBundle;
import edu.virginia.vcgr.appmgr.patch.builder.tree.PatchTree;
import edu.virginia.vcgr.appmgr.patch.builder.tree.restrictions.PatchRestrictionsDialog;
import edu.virginia.vcgr.appmgr.util.GUIUtilities;

public class ModifyPatchGroupRestrictionsAction extends AbstractPatchMenuAction
{
	static final long serialVersionUID = 0L;

	static private final String NAME = "Modify Restrictions";

	private JFrame _application;

	public ModifyPatchGroupRestrictionsAction(JFrame application, PatchTree tree)
	{
		super(NAME, tree);

		_application = application;
	}

	@Override
	protected boolean shouldBeEnabled(PatchTree tree)
	{
		TreePath[] paths = tree.getSelectionPaths();
		if (paths != null && paths.length == 1) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) paths[0].getLastPathComponent();
			return node.getLevel() == 1;
		}

		return false;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		TreePath[] paths = _tree.getSelectionPaths();
		if (paths != null && paths.length == 1) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) paths[0].getLastPathComponent();
			if (node.getLevel() == 1) {
				PatchBundle bundle = (PatchBundle) node.getUserObject();
				PatchRestrictionsDialog dialog = new PatchRestrictionsDialog(_application, bundle.getRestrictions());
				dialog.pack();
				dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
				GUIUtilities.centerWindow(dialog);
				dialog.setVisible(true);
				PatchRestrictions restrictions = dialog.getRestrictions();
				if (restrictions != null)
					bundle.setRestrictions(restrictions);
			}
		}
	}
}