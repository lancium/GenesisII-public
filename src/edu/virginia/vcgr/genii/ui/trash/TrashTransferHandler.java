package edu.virginia.vcgr.genii.ui.trash;

import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.TransferHandler;

import org.morgan.util.configuration.ConfigurationException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

class TrashTransferHandler extends TransferHandler
{
	static final long serialVersionUID = 0L;

	static private Log _logger = LogFactory.getLog(TrashTransferHandler.class);

	TrashTransferHandler()
	{
	}

	@Override
	protected Transferable createTransferable(JComponent c)
	{
		if (c instanceof JList)
		{
			Object []values = ((JList)c).getSelectedValues();
			if (values == null)
				values = new Object[0];
			
			TrashCanEntryWrapper []wrappers = 
				new TrashCanEntryWrapper[values.length];
			
			for (int lcv = 0; lcv < values.length; lcv++)
				wrappers[lcv] = (TrashCanEntryWrapper)values[lcv];
			
			return new TrashTransferable(wrappers);
		}
		
		return null;
	}

	@Override
	protected void exportDone(JComponent source, Transferable data, int action)
	{
		if (action == MOVE)
		{
			try
			{
				DefaultListModel model = (DefaultListModel)((JList)source).getModel();
				TrashCanEntryWrapper []wrappers = 
					(TrashCanEntryWrapper[])data.getTransferData(
						TrashTransferable.TRASH_TRANSFER_FLAVOR);
				for (TrashCanEntryWrapper wrapper : wrappers)
					model.removeElement(wrapper);
			}
			catch (IOException ioe)
			{
				throw new ConfigurationException(
					"Unable to finish drag-and-drop.", ioe);
			} 
			catch (UnsupportedFlavorException e)
			{
				throw new ConfigurationException(
					"Unable to finish drag-and-drop.", e);
			}
		}
	}

	@Override
	public int getSourceActions(JComponent c)
	{
		return MOVE;
	}

	@Override
	public boolean canImport(TransferSupport support)
	{
		if (!support.isDrop())
			return false;
		
		if (support.getComponent() instanceof JList)
		{
			if (!support.isDataFlavorSupported(
				TrashTransferable.TRASH_TRANSFER_FLAVOR))
				return false;
			
			if (support.getDropAction() == MOVE)
				return true;
			
			if ((support.getSourceDropActions() & MOVE) > 0)
			{
				support.setDropAction(MOVE);
				return true;
			}
		}
		
		return false;
	}

	@Override
	public boolean importData(TransferSupport support)
	{
		if (!support.isDrop())
			return false;
		
		if (support.getComponent() instanceof JList)
		{
			if (!support.isDataFlavorSupported(
				TrashTransferable.TRASH_TRANSFER_FLAVOR))
				return false;
			
			if (support.getDropAction() == MOVE)
			{
				try
				{
					TrashCanEntryWrapper []wrappers = 
						(TrashCanEntryWrapper[])support.getTransferable(
							).getTransferData(
								TrashTransferable.TRASH_TRANSFER_FLAVOR);
					
					ListModel lModel = ((JList)(support.getComponent())).getModel();
					DefaultListModel model = (DefaultListModel)lModel;
					for (TrashCanEntryWrapper wrapper : wrappers)
						model.addElement(wrapper);
					
					return true;
				}
				catch (IOException e)
				{
					throw new ConfigurationException(
						"Unable to drop data.", e);
				} 
				catch (UnsupportedFlavorException e)
				{
					throw new ConfigurationException(
							"Unable to drop data.", e);
				}
				catch (Throwable cause)
				{
					_logger.info("exception occurred in importData", cause);
				}
			}
		}
		
		return false;
	}
}
