package edu.virginia.vcgr.appmgr.patch.builder.tree.menu;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import edu.virginia.vcgr.appmgr.patch.builder.tree.PatchTree;

public class PatchMenu extends JPopupMenu
{
	static final long serialVersionUID = 0L;

	static private PatchMenu _menu = null;

	private PatchMenu(PatchTree tree)
	{
		super("Patch Menu");

		JFrame application = (JFrame) SwingUtilities.getRoot(tree);

		add(new JMenuItem(new RemovePatchOperationAction(tree)));
		addSeparator();
		add(new JMenuItem(new CreateNewPatchGroupAction(tree)));
		add(new JMenuItem(new ModifyPatchGroupRestrictionsAction(application, tree)));
	}

	static public PatchMenu getMenu(PatchTree tree)
	{
		if (_menu == null)
			_menu = new PatchMenu(tree);

		return _menu;
	}
}
