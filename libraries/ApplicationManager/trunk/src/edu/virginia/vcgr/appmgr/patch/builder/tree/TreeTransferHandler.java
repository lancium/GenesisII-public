package edu.virginia.vcgr.appmgr.patch.builder.tree;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public class TreeTransferHandler extends TransferHandler
{
	static final long serialVersionUID = 0L;

	static private final DataFlavor NODES_FLAVOR = NodesTransferable.NODES_FLAVOR;

	private DefaultMutableTreeNode[] _nodesToRemove;

	private boolean onlyAtomNodes(JTree tree)
	{
		TreePath[] paths = tree.getSelectionPaths();
		for (TreePath path : paths) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
			Object obj = node.getUserObject();
			if (obj != null && (obj instanceof AtomBundle))
				continue;

			return false;
		}

		return true;
	}

	private DefaultMutableTreeNode copy(TreeNode node)
	{
		DefaultMutableTreeNode old = (DefaultMutableTreeNode) node;
		return new DefaultMutableTreeNode(old.getUserObject(), old.getAllowsChildren());
	}

	@Override
	protected Transferable createTransferable(JComponent component)
	{
		JTree tree = (JTree) component;
		TreePath[] paths = tree.getSelectionPaths();

		if (paths != null) {
			// Make up a node array of copies for transfer and
			// another for/of the nodes that will be removed in
			// exportDone after a successful drop.
			List<DefaultMutableTreeNode> copies = new ArrayList<DefaultMutableTreeNode>();
			List<DefaultMutableTreeNode> toRemove = new ArrayList<DefaultMutableTreeNode>();

			for (TreePath path : paths) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
				if (node.getLevel() == 2) {
					copies.add(copy(node));
					toRemove.add(node);
				}
			}

			_nodesToRemove = toRemove.toArray(new DefaultMutableTreeNode[toRemove.size()]);
			return new NodesTransferable(copies.toArray(new DefaultMutableTreeNode[copies.size()]));
		}

		return null;
	}

	@Override
	protected void exportDone(JComponent source, Transferable data, int action)
	{
		if ((action & MOVE) == MOVE) {
			JTree tree = (JTree) source;
			DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
			// Remove nodes saved in nodesToRemove in createTransferable.
			for (DefaultMutableTreeNode node : _nodesToRemove)
				model.removeNodeFromParent(node);
		}
	}

	@Override
	public boolean canImport(TransferHandler.TransferSupport support)
	{
		if (!support.isDrop())
			return false;

		support.setShowDropLocation(true);
		if (!support.isDataFlavorSupported(NODES_FLAVOR))
			return false;

		// Do not allow a drop on the drag source selections
		JTree.DropLocation dropLocation = (JTree.DropLocation) support.getDropLocation();
		JTree tree = (JTree) support.getComponent();
		int dropRow = tree.getRowForPath(dropLocation.getPath());
		int[] selRows = tree.getSelectionRows();
		if (selRows == null)
			return false;

		for (int i = 0; i < selRows.length; i++)
			if (selRows[i] == dropRow)
				return false;

		tree.getPathForRow(dropRow);
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) tree.getPathForRow(dropRow).getLastPathComponent();
		if (parent.getLevel() == 1)
			return onlyAtomNodes(tree);

		return false;
	}

	@Override
	public int getSourceActions(JComponent c)
	{
		return MOVE;
	}

	@Override
	public boolean importData(TransferHandler.TransferSupport support)
	{
		if (!canImport(support))
			return false;

		// Extract transfer data
		DefaultMutableTreeNode[] nodes = null;
		try {
			Transferable t = support.getTransferable();
			nodes = (DefaultMutableTreeNode[]) t.getTransferData(NODES_FLAVOR);
		} catch (UnsupportedFlavorException ufe) {
			throw new RuntimeException("Unsupported transfer flavor.", ufe);
		} catch (IOException ioe) {
			throw new RuntimeException("I/O error during transfer.", ioe);
		}

		// Get drop location information.
		JTree.DropLocation dropLocation = (JTree.DropLocation) support.getDropLocation();
		int childIndex = dropLocation.getChildIndex();
		TreePath dest = dropLocation.getPath();
		MutableTreeNode parent = (MutableTreeNode) dest.getLastPathComponent();
		JTree tree = (JTree) support.getComponent();
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();

		// Configure for drop mode
		int index = childIndex; // DropMode.INSERT
		if (childIndex == -1)
			index = parent.getChildCount();

		// Add data to model
		for (DefaultMutableTreeNode node : nodes)
			model.insertNodeInto(node, parent, index);

		return true;
	}

	@Override
	public String toString()
	{
		return getClass().getName();
	}
}