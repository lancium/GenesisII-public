package edu.virginia.vcgr.genii.ui.rns.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Collection;
import java.util.Vector;

import org.morgan.util.Pair;
import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.ui.UIContext;
import edu.virginia.vcgr.genii.ui.rns.RNSTree;
import edu.virginia.vcgr.genii.ui.rns.RNSTreeNode;

public class RNSListTransferable implements Transferable
{
	static final private String FLAVOR_PATTERN = "%s;class=\"%s\"";
	
	static final public DataFlavor RNS_PATH_LIST_FLAVOR;
	
	static final public DataFlavor []SUPPORTED_FLAVORS;
	
	static
    {
		try
		{
			RNS_PATH_LIST_FLAVOR = new DataFlavor(String.format(
					FLAVOR_PATTERN, DataFlavor.javaJVMLocalObjectMimeType,
					RNSListTransferData.class.getName()));

			SUPPORTED_FLAVORS = new DataFlavor[] {
				RNS_PATH_LIST_FLAVOR
			};
		}
		catch (ClassNotFoundException cnfe)
		{
			throw new ConfigurationException(
				"Unable to create DnD transfer flavors.", cnfe);
		}
	};
	
	private RNSTree _tree;
	private UIContext _sourceContext;
	private Collection<Pair<RNSTreeNode, RNSPath>> _paths;
	
	RNSListTransferable(RNSTree tree, UIContext sourceContext,
		Collection<Pair<RNSTreeNode, RNSPath>> paths)
	{
		_sourceContext = sourceContext;
		_paths = new Vector<Pair<RNSTreeNode, RNSPath>>(paths);
		_tree = tree;
	}
	
	public UIContext getSourceContext()
	{
		return _sourceContext;
	}
	
	@Override
	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException
	{
		if (flavor.equals(RNS_PATH_LIST_FLAVOR))
		{
			return new RNSListTransferData(_tree, _sourceContext, _paths);
		} else
			throw new UnsupportedFlavorException(flavor);
	}

	@Override
	public DataFlavor[] getTransferDataFlavors()
	{
		return SUPPORTED_FLAVORS;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor)
	{
		for (DataFlavor test : SUPPORTED_FLAVORS)
			if (test.equals(flavor))
				return true;
		
		return false;
	}
}