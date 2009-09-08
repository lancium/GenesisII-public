package edu.virginia.vcgr.genii.ui.trash;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import org.morgan.util.configuration.ConfigurationException;

public class TrashTransferable implements Transferable
{
	static final private String FLAVOR_PATTERN = "%s;class=\"%s\"";
	
	static final DataFlavor TRASH_TRANSFER_FLAVOR;
	
	static
	{
		try
		{
			TRASH_TRANSFER_FLAVOR = new DataFlavor(String.format(
				FLAVOR_PATTERN, DataFlavor.javaJVMLocalObjectMimeType,
				TrashCanEntryWrapper[].class.getName()));
		}
		catch (ClassNotFoundException cnfe)
		{
			throw new ConfigurationException(
				"Unable to create trash can data flavor.", cnfe);
		}
	}
	
	private TrashCanEntryWrapper []_wrappers;
	
	TrashTransferable(TrashCanEntryWrapper []wrappers)
	{
		_wrappers = wrappers;
	}
	
	@Override
	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException
	{
		if (flavor.equals(TRASH_TRANSFER_FLAVOR))
			return _wrappers;
		
		throw new UnsupportedFlavorException(flavor);
	}

	@Override
	public DataFlavor[] getTransferDataFlavors()
	{
		return new DataFlavor[] { TRASH_TRANSFER_FLAVOR };
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor)
	{
		return flavor.equals(TRASH_TRANSFER_FLAVOR);
	}
}