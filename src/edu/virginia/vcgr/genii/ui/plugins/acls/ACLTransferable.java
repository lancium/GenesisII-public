package edu.virginia.vcgr.genii.ui.plugins.acls;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

import org.morgan.util.configuration.ConfigurationException;

public class ACLTransferable implements Transferable
{
	static final private String FLAVOR_PATTERN = "%s;class=\"%s\"";

	static public final DataFlavor DATA_FLAVOR;

	static {
		try {
			DATA_FLAVOR = new DataFlavor(String.format(FLAVOR_PATTERN, DataFlavor.javaJVMLocalObjectMimeType,
				ACLEntryWrapperTransferData.class.getName()));
		} catch (ClassNotFoundException e) {
			throw new ConfigurationException("Unable to create ACL transferable.", e);
		}
	}

	private ACLEntryWrapperTransferData _data;

	ACLTransferable(ACLEntryWrapperTransferData data)
	{
		_data = data;
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException
	{
		if (flavor.equals(DATA_FLAVOR))
			return _data;

		throw new UnsupportedFlavorException(flavor);
	}

	@Override
	public DataFlavor[] getTransferDataFlavors()
	{
		return new DataFlavor[] { DATA_FLAVOR };
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor)
	{
		return flavor.equals(DATA_FLAVOR);
	}
}