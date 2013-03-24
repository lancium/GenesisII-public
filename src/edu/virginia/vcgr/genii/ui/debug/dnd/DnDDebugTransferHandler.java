package edu.virginia.vcgr.genii.ui.debug.dnd;

import java.awt.Component;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.Pair;

import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.ui.UIContext;
import edu.virginia.vcgr.genii.ui.debug.DnDDebugTextArea;
import edu.virginia.vcgr.genii.ui.errors.ErrorHandler;
import edu.virginia.vcgr.genii.ui.rns.RNSTreeNode;
import edu.virginia.vcgr.genii.ui.rns.dnd.RNSListTransferData;
import edu.virginia.vcgr.genii.ui.rns.dnd.RNSListTransferable;

public class DnDDebugTransferHandler extends TransferHandler
{
	static final long serialVersionUID = 0L;

	static private Log _logger = LogFactory.getLog(DnDDebugTransferHandler.class);

	private UIContext _uiContext;

	public DnDDebugTransferHandler(UIContext uiContext)
	{
		_uiContext = uiContext;
	}

	@Override
	public boolean canImport(TransferSupport support)
	{
		Component comp = support.getComponent();

		if (comp instanceof DnDDebugTextArea) {
			// Accept only drag-and-drop, no cut/copy/paste
			if (!support.isDrop())
				return false;

			if (!support.isDataFlavorSupported(RNSListTransferable.RNS_PATH_LIST_FLAVOR))
				return false;

			boolean linkSupported = (support.getSourceDropActions() & TransferHandler.LINK) > 0;
			if (linkSupported) {
				support.setDropAction(TransferHandler.LINK);
				return true;
			}
		}

		return super.canImport(support);
	}

	@Override
	public boolean importData(TransferSupport support)
	{
		try {
			Component comp = support.getComponent();
			Transferable t = support.getTransferable();

			if (comp instanceof DnDDebugTextArea) {
				DnDDebugTextArea area = (DnDDebugTextArea) comp;
				RNSListTransferData data = (RNSListTransferData) t.getTransferData(RNSListTransferable.RNS_PATH_LIST_FLAVOR);
				for (Pair<RNSTreeNode, RNSPath> path : data.paths())
					area.append(path.second().pwd() + "\n");
				area.append("\n");

				return true;
			}
		} catch (IOException ioe) {
			_logger.warn("Unable to perform drag-and-drop or cut/copy/paste action.", ioe);
			ErrorHandler.handleError(_uiContext, (JComponent) support.getComponent(), ioe);
		} catch (UnsupportedFlavorException e) {
			_logger.warn("Unable to perform drag-and-drop or cut/copy/paste action.", e);
			ErrorHandler.handleError(_uiContext, (JComponent) support.getComponent(), e);
		}

		return super.importData(support);
	}
}