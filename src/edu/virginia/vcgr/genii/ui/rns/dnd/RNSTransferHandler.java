package edu.virginia.vcgr.genii.ui.rns.dnd;

import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.TreePath;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.Pair;

import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.ui.UIContext;
import edu.virginia.vcgr.genii.ui.errors.ErrorHandler;
import edu.virginia.vcgr.genii.ui.rns.RNSFilledInTreeObject;
import edu.virginia.vcgr.genii.ui.rns.RNSTree;
import edu.virginia.vcgr.genii.ui.rns.RNSTreeCopier;
import edu.virginia.vcgr.genii.ui.rns.RNSTreeLinker;
import edu.virginia.vcgr.genii.ui.rns.RNSTreeMover;
import edu.virginia.vcgr.genii.ui.rns.RNSTreeNode;
import edu.virginia.vcgr.genii.ui.rns.RNSTreeObject;
import edu.virginia.vcgr.genii.ui.rns.RNSTreeObjectType;
import edu.virginia.vcgr.genii.ui.rns.RNSTreeOperator;

public class RNSTransferHandler extends TransferHandler
{
	static final long serialVersionUID = 0L;

	static private Log _logger = LogFactory.getLog(RNSTransferHandler.class);
	
	private UIContext _uiContext;
	
	public RNSTransferHandler(UIContext uiContext)
	{
		_uiContext = uiContext;
	}
	
	@Override
	public boolean canImport(TransferSupport support)
	{
		Component comp = support.getComponent();
		
		if (comp instanceof RNSTree)
		{
			// Accept only drag-and-drop, no cut/copy/paste
			if (!support.isDrop())
				return false;
			
			JTree.DropLocation dl = (JTree.DropLocation)support.getDropLocation();
			if (dl == null)
				return false;
			
			if (dl.getPath() == null)
				return false;
			
			RNSTreeNode node = (RNSTreeNode)dl.getPath().getLastPathComponent();
			if (((RNSTreeObject)node.getUserObject()).objectType() 
				!= RNSTreeObjectType.ENDPOINT_OBJECT)
				return false;
			
			RNSPath targetParent =
				((RNSFilledInTreeObject)node.getUserObject()).path();
			
			TypeInformation tInfo;
			
			try
			{
				tInfo = new TypeInformation(targetParent.getEndpoint());
			}
			catch (Throwable cause)
			{
				return false;
			}
			
			if (!tInfo.isRNS())
				return false;
			
			if (tInfo.isExport())
				support.setDropAction(COPY);
			
			if (support.isDataFlavorSupported(
				RNSListTransferable.RNS_PATH_LIST_FLAVOR))
			{
				try
				{
					Collection<Pair<RNSTreeNode, RNSPath>> paths = null;	
					Transferable t = support.getTransferable();
					
					RNSListTransferData data = 
						(RNSListTransferData)t.getTransferData(
							RNSListTransferable.RNS_PATH_LIST_FLAVOR);
					paths = data.paths();
					
					for (Pair<RNSTreeNode, RNSPath> path : paths)
					{
						if (path.second().equals(targetParent))
							return false;
						
						if (path.second().getParent().equals(targetParent))
							return false;
					}
				}
				catch (Exception e)
				{
					// Do nothing
				}
				
				if (support.getDropAction() == COPY)
					return true;
				
				if ( (support.getDropAction() == LINK || 
						support.getDropAction() == MOVE) &&
						!tInfo.isExport())
					return true;
				
				if ( (support.getSourceDropActions() & COPY) > 0)
					return true;
				
				if (tInfo.isExport())
					return false;
				
				boolean actionSupported = 
					(support.getSourceDropActions() & 
						(LINK | MOVE | COPY)) > 0;
				if (actionSupported)
					return true;
			} else if (support.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
			{
				if (support.getDropAction() == COPY)
					return true;
				
				if ((support.getSourceDropActions() & COPY) > 0)
				{
					support.setDropAction(COPY);
					return true;
				}
			}
		}
		
		return false;
	}

	@Override
	protected Transferable createTransferable(JComponent c)
	{
		if (c instanceof RNSTree)
		{
			RNSTree tree = (RNSTree)c;
			TreePath []paths = tree.getSelectionPaths();
			if (paths != null)
			{
				Collection<Pair<RNSTreeNode, RNSPath>> rnsPaths =
					new Vector<Pair<RNSTreeNode, RNSPath>>(
					paths.length);
				for (TreePath path : paths)
				{
					TreePath parentPath = path.getParentPath();
					RNSTreeNode parentNode = 
						(parentPath == null) ? null :
							(RNSTreeNode)parentPath.getLastPathComponent();
					
					RNSTreeNode node =
						(RNSTreeNode)path.getLastPathComponent();
					RNSTreeObject obj = (RNSTreeObject)node.getUserObject();
					if (obj.objectType() == RNSTreeObjectType.ENDPOINT_OBJECT)
					{
						RNSFilledInTreeObject rnsEndpointObject =
							(RNSFilledInTreeObject)obj;
						rnsPaths.add(new Pair<RNSTreeNode, RNSPath>(
							parentNode,
							rnsEndpointObject.path()));
					}
				}
				
				if (rnsPaths.size() > 0)
					return new RNSListTransferable((RNSTree)c, _uiContext, rnsPaths);
			}
		}
		
		return super.createTransferable(c);
	}

	@Override
	protected void exportDone(JComponent source, Transferable data, int action)
	{
		// We ignore this because the operations take too long
		// in general and we will get notified through an out-of-bounds
		// mechanism.
	}

	@Override
	public int getSourceActions(JComponent c)
	{
		if (c instanceof RNSTree)
			return COPY | MOVE | LINK;
		
		return super.getSourceActions(c);
	}

	@Override
	public boolean importData(TransferSupport support)
	{
		RNSTreeOperator operator = null;
	
		try
		{
			Component comp = support.getComponent();
			Transferable t = support.getTransferable();
			
			if (comp instanceof RNSTree)
			{
				JTree.DropLocation dl = 
					(JTree.DropLocation)support.getDropLocation();
				
				if (support.isDataFlavorSupported(
					RNSListTransferable.RNS_PATH_LIST_FLAVOR))
				{
					RNSListTransferData data = 
						(RNSListTransferData)t.getTransferData(
							RNSListTransferable.RNS_PATH_LIST_FLAVOR);
					
					int action = support.getDropAction();
					switch (action)
					{
						case MOVE :
							operator = RNSTreeMover.move(data.tree(), (RNSTree)comp,
								dl.getPath(), data.sourceContext(), data.paths());
							break;
						case LINK :
							operator = RNSTreeLinker.link(data.tree(), (RNSTree)comp,
								dl.getPath(), data.sourceContext(), data.paths());
							break;
						case COPY :
							operator = RNSTreeCopier.copy(data.tree(), (RNSTree)comp,
								dl.getPath(), data.sourceContext(), data.paths());
							break;
						default :
							return false;
					}
				} else if (support.isDataFlavorSupported(
					DataFlavor.javaFileListFlavor))
				{
					List<?> files = (List<?>)t.getTransferData(
						DataFlavor.javaFileListFlavor);
					
					int action = support.getDropAction();
					switch (action)
					{
						case COPY :
							operator = RNSTreeCopier.copy((RNSTree)comp,
								dl.getPath(), _uiContext, files);
							break;
						default :
							return false;
					}
				}
				
				if (operator != null)
					return operator.performOperation();
			}
		}
		catch (IOException ioe)
		{
			_logger.warn(
				"Unable to perform drag-and-drop or cut/copy/paste action.", 
				ioe);
			ErrorHandler.handleError(_uiContext,
				(JComponent)support.getComponent(), ioe);
		}
		catch (UnsupportedFlavorException e)
		{
			_logger.warn(
				"Unable to perform drag-and-drop or cut/copy/paste action.", 
				e);
			ErrorHandler.handleError(_uiContext,
				(JComponent)support.getComponent(), e);
		}
		
		return false;
	}
}