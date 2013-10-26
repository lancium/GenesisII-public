package edu.virginia.vcgr.appmgr.patch.builder.tree.menu;

import javax.swing.AbstractAction;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import edu.virginia.vcgr.appmgr.patch.builder.tree.PatchTree;

abstract class AbstractPatchMenuAction extends AbstractAction implements TreeSelectionListener
{
	static final long serialVersionUID = 0L;

	protected PatchTree _tree;

	protected abstract boolean shouldBeEnabled(PatchTree tree);

	protected AbstractPatchMenuAction(String name, PatchTree tree)
	{
		super(name);

		_tree = tree;
		_tree.addTreeSelectionListener(this);

		setEnabled(shouldBeEnabled(_tree));
	}

	@Override
	public void valueChanged(TreeSelectionEvent e)
	{
		setEnabled(shouldBeEnabled(_tree));
	}
}