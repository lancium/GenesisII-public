package edu.virginia.vcgr.appmgr.patch.builder.tree;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Map;

import javax.swing.DropMode;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import edu.virginia.vcgr.appmgr.patch.builder.PatchAtom;
import edu.virginia.vcgr.appmgr.patch.builder.PatchRC;
import edu.virginia.vcgr.appmgr.patch.builder.tree.menu.PatchMenu;

public class PatchTree extends JTree
{
	static final long serialVersionUID = 0L;

	static private void fillInTree(DefaultTreeModel model, Map<String, PatchAtom> atoms)
	{
		DefaultMutableTreeNode root = new DefaultMutableTreeNode();
		DefaultMutableTreeNode patch = new DefaultMutableTreeNode(new PatchBundle("default"), true);
		root.add(patch);
		String[] keys = atoms.keySet().toArray(new String[atoms.size()]);
		Arrays.sort(keys);
		for (String key : keys) {
			patch.add(new DefaultMutableTreeNode(new AtomBundle(key, atoms.get(key)), false));
		}

		model.setRoot(root);
	}

	private class MouseListenerImpl extends MouseAdapter
	{
		@Override
		public void mousePressed(MouseEvent e)
		{
			if (e.isPopupTrigger()) {
				PatchMenu menu = PatchMenu.getMenu(PatchTree.this);
				menu.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}

	public PatchTree(PatchRC rc, Map<String, PatchAtom> atoms)
	{
		fillInTree((DefaultTreeModel) getModel(), atoms);
		setDragEnabled(true);
		setDropMode(DropMode.ON);
		setTransferHandler(new TreeTransferHandler());
		setRootVisible(false);
		setShowsRootHandles(true);
		setCellRenderer(new TreeCellRenderImpl(rc));
		setAutoscrolls(true);
		addMouseListener(new MouseListenerImpl());
	}
}
