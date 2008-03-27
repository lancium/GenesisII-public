package edu.virginia.vcgr.genii.client.gui.widgets.rns;

import java.awt.Cursor;
import java.awt.Insets;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.Autoscroll;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.byteio.ByteIOStreamFactory;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSMultiLookupResultException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;

public class RNSTree extends JTree implements Autoscroll
{
	static final long serialVersionUID = 0L;

	static Log _logger = LogFactory.getLog(RNSTree.class);
	
	// These insets and ints are for the autoscroll feature for drag and drop.
	private Insets _insets = null;
	private int _top = 0, _bottom = 0, _topRow = 0, _bottomRow = 0;
	
	public RNSTree(RNSPath root) throws RNSException
	{
		super(new RNSTreeModel(root.createSandbox()));
		
		DragSource dragSource = DragSource.getDefaultDragSource();
		dragSource.createDefaultDragGestureRecognizer(this,
			DnDConstants.ACTION_COPY | DnDConstants.ACTION_LINK,
			new RNSTreeDragGestureListener());
		new DropTarget(this, new RNSTreeDropTargetListener());
		
		setShowsRootHandles(true);
		
		RNSTreeModel model = (RNSTreeModel)getModel();
		model.setTree(this);
		model.prepareExpansion((RNSTreeNode)model.getRoot());
		addTreeWillExpandListener(model);
		addTreeExpansionListener(model);
	}
	
	public RNSTree() throws ConfigurationException, RNSException
	{
		this(RNSPath.getCurrent().getRoot());
	}
	
	public Insets getAutoscrollInsets()
	{
		return _insets;
	}
	
	public void autoscroll(Point p)
	{
		// Only support up/down scrolling
		_top = Math.abs(getLocation().y) + 10;
		_bottom = _top + getParent().getHeight() - 20;
		int next;
		if (p.y < _top)
		{
			next = _topRow--;
			_bottomRow++;
			scrollRowToVisible(next);
		} else if (p.y > _bottom)
		{
			next = _bottomRow++;
			_topRow--;
			scrollRowToVisible(next);
		}
	}
	
	@SuppressWarnings("unchecked")
	public boolean reloadSubtree(RNSPath path)
	{
		RNSTreeModel model = (RNSTreeModel)getModel();
		TreeNode root = (TreeNode)model.getRoot();
		String sPath = path.pwd();
		
		String []components = sPath.substring(1).split("/");
		outer:
		for (String component : components)
		{
			Enumeration<TreeNode> children = root.children();
			while (children.hasMoreElements())
			{
				TreeNode child = children.nextElement();
				if (child.toString().equals(component))
				{
					root = child;
					continue outer;
				}
			}
			
			return false;
		}
		
		if (root instanceof RNSTreeNode)
			((RNSTreeNode)root).refresh(model);
		
		model.reload(root);
		return true;
	}
	
	private static class RNSTreeDragGestureListener 
		implements DragGestureListener
	{
		public void dragGestureRecognized(DragGestureEvent event)
		{
			try
			{
				Collection<RNSPath> rPaths = new ArrayList<RNSPath>();
				
				JTree tree = (JTree)event.getComponent();
				TreePath []paths = tree.getSelectionPaths();
				if (paths == null || paths.length == 0)
				{
					// Nothing selected, nothing to drag
				} else
				{
					Cursor cursor = null;
					
					int action = event.getDragAction();
					if ((action & DnDConstants.ACTION_COPY) != 0)
					{
						cursor = DragSource.DefaultCopyDrop;
						
						// Only files.
						for (TreePath path : paths)
						{
							DefaultMutableTreeNode node = 
								(DefaultMutableTreeNode)path.getLastPathComponent();
							if (!(node instanceof RNSTreeNode))
							{
								_logger.fatal("Cannot drag-and-drop un-resolved nodes.");
								tree.getToolkit().beep();
							} else
							{
								RNSTreeNode rNode = (RNSTreeNode)node;
								RNSPath rnsPath = rNode.getRNSPath();
								if (!rnsPath.exists())
								{
									_logger.fatal("Cannot drag-and-drop un-resolved nodes.");
									tree.getToolkit().beep();
								}
								
								TypeInformation typeInfo = new TypeInformation(rnsPath.getEndpoint());
								if (!typeInfo.isByteIO())
								{
									_logger.fatal("Can only copy ByteIO instances.");
									tree.getToolkit().beep();
								}
								
								rPaths.add(rnsPath);
							}
						}
					} else if ((action & DnDConstants.ACTION_LINK) != 0)
					{
						cursor = DragSource.DefaultLinkDrop;
						
						// Anything.
						for (TreePath path : paths)
						{
							DefaultMutableTreeNode node = 
								(DefaultMutableTreeNode)path.getLastPathComponent();
							if (!(node instanceof RNSTreeNode))
							{
								_logger.fatal("Cannot drag-and-drop un-resolved nodes.");
								tree.getToolkit().beep();
							} else
							{
								RNSTreeNode rNode = (RNSTreeNode)node;
								RNSPath rnsPath = rNode.getRNSPath();
								if (!rnsPath.exists())
								{
									_logger.fatal("Cannot drag-and-drop un-resolved nodes.");
									tree.getToolkit().beep();
								}
								
								rPaths.add(rnsPath);
							}
						}
					} else
					{
						// Unknown action
						_logger.error("Unknown drag-and-drop action.");
						tree.getToolkit().beep();
					}
					
					RNSTransferableTreeNode tNode =
						new RNSTransferableTreeNode(rPaths);
					event.startDrag(cursor, tNode, new MyDragSourceListener());
				}
			}
			catch (RNSPathDoesNotExistException dne)
			{
				_logger.fatal("Unexpected exception -- path does not exist in tree.", dne);
			}
		}
	}
	
	private class RNSTreeDropTargetListener implements DropTargetListener
	{
		public void dragEnter(DropTargetDragEvent dropTargetDragEvent)
		{
			// Setup positioning info for auto-scrolling
			_top = Math.abs(getLocation().y);
			_bottom = _top + getParent().getHeight();
			_topRow = getClosestRowForLocation(0, _top);
			_bottomRow = getClosestRowForLocation(0, _bottom);
			_insets = new Insets(_top + 10, 0, _bottom - 10, getWidth());
		}
		
		public void dragExit(DropTargetEvent event)
		{
		}
		
		public void dragOver(DropTargetDragEvent event)
		{
		}
		
		public void dropActionChanged(DropTargetDragEvent event)
		{
		}
		
		@SuppressWarnings("unchecked")
		synchronized public void drop(DropTargetDropEvent event)
		{
			Point location = event.getLocation();
			TreePath path = getPathForLocation(location.x, location.y);
			Object node = path.getLastPathComponent();
			if (node == null || !(node instanceof TreeNode))
			{
				_logger.fatal("Couldn't find drop target.");
				event.rejectDrop();
			}
			
			try
			{
				Transferable tr = event.getTransferable();
				if (tr.isDataFlavorSupported(RNSTransferableTreeNode.DEFAULT_RNS_PATHS_FLAVOR))
				{
					event.acceptDrop(DnDConstants.ACTION_COPY | DnDConstants.ACTION_LINK);
					Object userObject = tr.getTransferData(
						RNSTransferableTreeNode.DEFAULT_RNS_PATHS_FLAVOR);
					if ((event.getDropAction() & DnDConstants.ACTION_COPY) != 0)
						copy(path, (Collection<RNSPath>)userObject);
					else
						link(path, (Collection<RNSPath>)userObject);
					event.dropComplete(true);
				} else if (tr.isDataFlavorSupported(DataFlavor.stringFlavor))
				{
					event.acceptDrop(DnDConstants.ACTION_COPY);
					String stringFlavor = (String)tr.getTransferData(DataFlavor.stringFlavor);
					
					BufferedReader reader = new BufferedReader(new StringReader(stringFlavor));
					String line;
					List<File> fileList = new ArrayList<File>();
					while ( (line = reader.readLine()) != null)
					{
						line = line.trim();
						if (line.length() > 0)
						{
							URI uri = URI.create(line);
							fileList.add(new File(uri));
						}
					}
					copy(path, fileList);
					event.dropComplete(true);
				} else if (tr.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
				{
					event.acceptDrop(DnDConstants.ACTION_COPY);
					List<File> fileList = (List<File>)tr.getTransferData(
						DataFlavor.javaFileListFlavor);
					copy(path, fileList);
					event.dropComplete(true);
				} else
				{
					_logger.error("Unable to accept drop.");
					event.rejectDrop();
				}
			}
			catch (IOException ioe)
			{
				_logger.error("IOException during drag-and-drop operation.", ioe);
				event.rejectDrop();
			}
			catch (UnsupportedFlavorException ufe)
			{
				_logger.error("Unsupported drag and drop flavor.", ufe);
				event.rejectDrop();
			}
		}
		
		private RNSPath getNewPath(RNSPath parent, String origName)
			throws IOException
		{
			int lcv = 0;
			
			for (lcv = 0; lcv < 10; lcv++)
			{
				String newName = null;
				
				try
				{
					newName = (lcv == 0) ? 
						origName : String.format("%s (%d)", origName, lcv);
					RNSPath ret = parent.lookup(newName, RNSPathQueryFlags.DONT_CARE);
					if (!ret.exists())
						return ret;
				}
				catch (RNSMultiLookupResultException rme)
				{
					_logger.warn("Multi lookup exception -- pattern \"" + 
						newName + "\" matched too many names.", rme);
				}
				catch (RNSException re)
				{
					_logger.error("Generic RNSException occurred.", re);
					throw new IOException(
						"Unable to talk to target grid directory.", re);
				}
			}
			
			throw new IOException("Unable to create new name for \"" 
				+ origName + "\".");
		}
		

		public void copy(InputStream in, OutputStream out)
			throws IOException
		{
			byte []data = new byte[ByteIOConstants.PREFERRED_SIMPLE_XFER_BLOCK_SIZE];
			
			while (true)
			{
				int read = in.read(data);
				if (read <= 0)
					break;
				out.write(data, 0, read);
			}
		}
		
		private void copy(TreePath path, Collection<RNSPath> rnsPaths)
			throws IOException
		{
			DefaultMutableTreeNode parent =
				(DefaultMutableTreeNode)path.getLastPathComponent();
			if (!(parent instanceof RNSTreeNode))
			{
				_logger.fatal("Cannot drop onto an un-resolved RNS path.");
				throw new IOException("Unable to drop onto un-resolved RNS path.");
			}
			
			RNSTreeNode rNode = (RNSTreeNode)parent;
			
			for (RNSPath rPath : rnsPaths)
			{
				RNSPath newPath = getNewPath(rNode.getRNSPath(),
					rPath.getName());
				InputStream in = null;
				OutputStream out = null;
				
				try
				{
					in = ByteIOStreamFactory.createInputStream(rPath);
					out = ByteIOStreamFactory.createOutputStream(newPath);
					copy(in, out);
				}
				catch (Exception e)
				{
					_logger.error("Unable to copy files.", e);
					if (e instanceof IOException)
						throw (IOException)e;
					throw new IOException("Unable to copy files.", e);
				}
				finally
				{
					StreamUtils.close(in);
					StreamUtils.close(out);
				}
			}
			
			rNode.refresh((RNSTreeModel)getModel());
		}
		
		private void link(TreePath path, Collection<RNSPath> rnsPaths)
			throws IOException
		{
			DefaultMutableTreeNode parent =
				(DefaultMutableTreeNode)path.getLastPathComponent();
			if (!(parent instanceof RNSTreeNode))
			{
				_logger.fatal("Cannot drop onto an un-resolved RNS path.");
				throw new IOException("Unable to drop onto un-resolved RNS path.");
			}
			
			RNSTreeNode rNode = (RNSTreeNode)parent;
		
			for (RNSPath rPath : rnsPaths)
			{
				RNSPath newPath = getNewPath(
					rNode.getRNSPath(), rPath.getName());
				
				try
				{
					newPath.link(rPath.getEndpoint());
				}
				catch (RNSException re)
				{
					_logger.warn("Unable to link in new path.", re);
				}
			}
			
			rNode.refresh((RNSTreeModel)getModel());
		}
		
		private void copy(TreePath path, List<File> fileList)
			throws IOException
		{
			DefaultMutableTreeNode parent =
				(DefaultMutableTreeNode)path.getLastPathComponent();
			if (!(parent instanceof RNSTreeNode))
			{
				_logger.fatal("Cannot drop onto an un-resolved RNS path.");
				throw new IOException("Unable to drop onto un-resolved RNS path.");
			}
			
			RNSTreeNode rNode = (RNSTreeNode)parent;
			
			for (File file : fileList)
			{
				RNSPath newPath = getNewPath(rNode.getRNSPath(),
					file.getName());
				InputStream in = null;
				OutputStream out = null;
				
				try
				{
					in = new FileInputStream(file);
					out = ByteIOStreamFactory.createOutputStream(newPath);
					copy(in, out);
				}
				catch (Exception e)
				{
					_logger.error("Unable to copy files.", e);
					if (e instanceof IOException)
						throw (IOException)e;
					throw new IOException("Unable to copy files.", e);
				}
				finally
				{
					StreamUtils.close(in);
					StreamUtils.close(out);
				}
			}
			
			rNode.refresh((RNSTreeModel)getModel());
		}
	}
	
	static private class MyDragSourceListener implements DragSourceListener
	{
		public void dragDropEnd(DragSourceDropEvent event)
		{
		}
		
		public void dragEnter(DragSourceDragEvent event)
		{
			DragSourceContext context =
				event.getDragSourceContext();
			int dropAction = event.getDropAction();
			if ((dropAction & DnDConstants.ACTION_COPY) != 0)
				context.setCursor(DragSource.DefaultCopyDrop);
			else if ((dropAction & DnDConstants.ACTION_LINK) != 0)
				context.setCursor(DragSource.DefaultLinkDrop);
			else
				context.setCursor(DragSource.DefaultCopyNoDrop);
		}
		
		public void dragExit(DragSourceEvent event)
		{
		}
		
		public void dragOver(DragSourceDragEvent event)
		{
		}
		
		public void dropActionChanged(DragSourceDragEvent event)
		{
		}
	}
}

class RNSTransferableTreeNode
	extends DefaultMutableTreeNode implements Transferable
{
	static final long serialVersionUID = 0L;
		
	final public static DataFlavor DEFAULT_RNS_PATHS_FLAVOR =
		new DataFlavor(DefaultMutableTreeNode.class,
			"RNS Paths");
	
	private Collection<RNSPath> _data;
	
	public RNSTransferableTreeNode(Collection<RNSPath> data)
	{
		_data = data;
	}
	
	public DataFlavor[] getTransferDataFlavors()
	{
		return new DataFlavor[] { DEFAULT_RNS_PATHS_FLAVOR };
	}
	
	public Object getTransferData(DataFlavor flavor)
		throws UnsupportedFlavorException, IOException
	{
		if (flavor.equals(DEFAULT_RNS_PATHS_FLAVOR))
			return _data;
		else
			throw new UnsupportedFlavorException(flavor);
	}
	
	public boolean isDataFlavorSupported(DataFlavor flavor)
	{
		return flavor.equals(DEFAULT_RNS_PATHS_FLAVOR);
	}
}