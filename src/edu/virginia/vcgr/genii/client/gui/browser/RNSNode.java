package edu.virginia.vcgr.genii.client.gui.browser;

import java.rmi.RemoteException;
import java.util.Date;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.ggf.rns.Add;
import org.ggf.rns.CreateFile;
import org.ggf.rns.EntryType;
import org.ggf.rns.List;
import org.ggf.rns.ListResponse;
import org.ggf.rns.RNSPortType;
import org.morgan.util.configuration.ConfigurationException;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;

public class RNSNode extends DefaultMutableTreeNode
{
	static private final long _DEFAULT_CACHE_TIMEOUT = 1000 * 30;
	static final long serialVersionUID = 0L;
	
	private Date _lastExpanded = null;
	
	public RNSNode(RNSPath path)
		throws RNSPathDoesNotExistException
	{
		this(new RNSEntry(path));
	}
	
	public RNSNode(String name, EndpointReferenceType target)
	{
		this(new RNSEntry(name, target));
	}
	
	private RNSNode(RNSEntry entry)
	{
		super(entry);
		
		setAllowsChildren(entry.getTypeInformation().isRNS());
	}
	
	public void preExpand(JTree tree)
		throws RemoteException, ConfigurationException
	{
		if (_lastExpanded == null || 
			(new Date().getTime() - _lastExpanded.getTime()) > 
				_DEFAULT_CACHE_TIMEOUT)
		{
			fillInChildren(tree);
			_lastExpanded = new Date();
		}
	}
	
	private void fillInChildren(JTree tree)
		throws ConfigurationException, RemoteException
	{
		removeAllChildren();
		add(new DefaultMutableTreeNode("... expanding ...", false));
		
		new Thread(new Poller(tree)).start();
	}
	
	private void setChildren(JTree tree, ListResponse response)
	{
		removeAllChildren();
		for (EntryType listEntry : response.getEntryList())
		{
			add(new RNSNode(listEntry.getEntry_name(), 
				listEntry.getEntry_reference()));
		}
		
		_lastExpanded = new Date();
		((DefaultTreeModel)tree.getModel()).reload(this);
	}
	
	private class Poller implements Runnable
	{
		private JTree _tree;
		
		public Poller(JTree tree)
		{
			_tree = tree;
		}
		
		public void run()
		{
			try
			{
				RNSEntry entry = (RNSEntry)getUserObject();
				
				RNSPortType rpt = ClientUtils.createProxy(RNSPortType.class, 
					entry.getTarget());
				ListResponse resp = rpt.list(new List(".*"));
				SwingUtilities.invokeLater(new Inserter(_tree, resp));
			}
			catch (Throwable t)
			{
				t.printStackTrace(System.err);
			}
		}
	}
	
	private class Inserter implements Runnable
	{
		private JTree _tree;
		private ListResponse _response;
		
		public Inserter(JTree tree, ListResponse response)
		{
			_tree = tree;
			_response = response;
		}
		
		public void run()
		{
			setChildren(_tree, _response);
		}
	}
	
	public void refresh(JTree tree)
		throws ConfigurationException, RemoteException
	{
		_lastExpanded = null;
		preExpand(tree);

		((DefaultTreeModel)tree.getModel()).reload(this);
	}
	
	public void addDirectory(JTree tree, String newDirectoryName)
		throws ConfigurationException, RemoteException
	{
		RNSEntry entry = (RNSEntry)getUserObject();
		RNSPortType rpt = ClientUtils.createProxy(RNSPortType.class, 
			entry.getTarget());
		addComponent(tree, newDirectoryName,
			rpt.add(new Add(
				newDirectoryName, null, null)).getEntry_reference());
	}
	
	public void addFile(JTree tree, String newFileName)
		throws ConfigurationException, RemoteException
	{
		RNSEntry entry = (RNSEntry)getUserObject();
		RNSPortType rpt = ClientUtils.createProxy(RNSPortType.class, 
			entry.getTarget());
		addComponent(tree, newFileName,
			rpt.createFile(new CreateFile(newFileName)).getEntry_reference());
	}
	
	private void addComponent(JTree tree, String name,
		EndpointReferenceType newEntry)
	{
		add(new RNSNode(name, newEntry));
		((DefaultTreeModel)tree.getModel()).reload(this);
	}
}