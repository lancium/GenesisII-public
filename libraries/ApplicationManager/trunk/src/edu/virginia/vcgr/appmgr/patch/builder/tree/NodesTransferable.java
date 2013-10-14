package edu.virginia.vcgr.appmgr.patch.builder.tree;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.tree.DefaultMutableTreeNode;

public class NodesTransferable implements Transferable
{
	static public final DataFlavor NODES_FLAVOR;
	static public final DataFlavor[] FLAVORS;

	static {
		try {
			NODES_FLAVOR =
				new DataFlavor(String.format("%s;class=\"%s\"", DataFlavor.javaJVMLocalObjectMimeType,
					DefaultMutableTreeNode[].class.getName()));
			FLAVORS = new DataFlavor[] { NODES_FLAVOR };
		} catch (ClassNotFoundException cnfe) {
			throw new RuntimeException("Unable to initialize data flavors.", cnfe);
		}
	}

	private DefaultMutableTreeNode[] _nodes;

	public NodesTransferable(DefaultMutableTreeNode[] nodes)
	{
		_nodes = nodes;
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException
	{
		if (!isDataFlavorSupported(flavor))
			throw new UnsupportedFlavorException(flavor);

		return _nodes;
	}

	@Override
	public DataFlavor[] getTransferDataFlavors()
	{
		return FLAVORS;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor)
	{
		return NODES_FLAVOR.equals(flavor);
	}
}