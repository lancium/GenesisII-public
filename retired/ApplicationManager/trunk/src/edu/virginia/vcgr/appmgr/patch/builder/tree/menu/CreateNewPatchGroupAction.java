package edu.virginia.vcgr.appmgr.patch.builder.tree.menu;

import java.awt.event.ActionEvent;
import java.util.Enumeration;

import javax.swing.JOptionPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import edu.virginia.vcgr.appmgr.patch.builder.tree.PatchBundle;
import edu.virginia.vcgr.appmgr.patch.builder.tree.PatchTree;

public class CreateNewPatchGroupAction extends AbstractPatchMenuAction
{
	static final long serialVersionUID = 0L;

	static private final String NAME = "Create Patch Group";

	public CreateNewPatchGroupAction(PatchTree tree)
	{
		super(NAME, tree);
	}

	@Override
	protected boolean shouldBeEnabled(PatchTree tree)
	{
		return true;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		int index = 0;
		String result = JOptionPane.showInputDialog(_tree, "Enter a name for the new patch group?");
		if (result == null)
			return;

		DefaultTreeModel model = (DefaultTreeModel) _tree.getModel();
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
		Enumeration<?> children = root.children();
		while (children.hasMoreElements()) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) children.nextElement();
			String name = node.toString();
			if (name.equals(result)) {
				JOptionPane.showMessageDialog(_tree, String.format("The patch group \"%s\" already exists!", result),
					"Patch Group Exists", JOptionPane.ERROR_MESSAGE);
				return;
			}

			if (name.compareTo(result) < 0)
				index++;
		}

		model.insertNodeInto(new DefaultMutableTreeNode(new PatchBundle(result), true), root, index);
	}
}