package edu.virginia.vcgr.genii.ui.plugins.acls;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.DropMode;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.TransferHandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.Pair;
import org.morgan.util.io.StreamUtils;
import org.morgan.utils.gui.GUIUtils;
import org.morgan.utils.gui.tearoff.TearoffHandler;

import edu.virginia.vcgr.genii.client.common.GenesisIIBaseRP;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyManager;
import edu.virginia.vcgr.genii.client.security.authz.acl.Acl;
import edu.virginia.vcgr.genii.client.security.authz.acl.AclAuthZClientTool;
import edu.virginia.vcgr.genii.client.security.authz.acl.AclEntry;
import edu.virginia.vcgr.genii.common.security.AuthZConfig;
import edu.virginia.vcgr.genii.security.credentials.identity.X509Identity;
import edu.virginia.vcgr.genii.ui.ApplicationContext;
import edu.virginia.vcgr.genii.ui.UIContext;
import edu.virginia.vcgr.genii.ui.errors.ErrorHandler;
import edu.virginia.vcgr.genii.ui.plugins.LazyLoadTabHandler;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginContext;
import edu.virginia.vcgr.genii.ui.progress.AbstractTask;
import edu.virginia.vcgr.genii.ui.progress.Task;
import edu.virginia.vcgr.genii.ui.progress.TaskCompletionListener;
import edu.virginia.vcgr.genii.ui.progress.TaskProgressListener;
import edu.virginia.vcgr.genii.ui.rns.RNSTreeNode;
import edu.virginia.vcgr.genii.ui.rns.dnd.RNSListTransferData;
import edu.virginia.vcgr.genii.ui.rns.dnd.RNSListTransferable;
import edu.virginia.vcgr.genii.ui.utils.SimplePanel;

class ACLPanel extends JPanel implements LazyLoadTabHandler
{
	static final long serialVersionUID = 0L;
	
	static private Log _logger = LogFactory.getLog(ACLPanel.class);
	
	private ApplicationContext _appContext;
	private UIContext _uiContext;
	
	private RNSPath _targetPath;
	
	private Acl _acl = null;
	
	private ACLList _readList = new ACLList(new DeleteAction());
	private ACLList _writeList = new ACLList(new DeleteAction());
	private ACLList _executeList = new ACLList(new DeleteAction());
	
	private ACLPanel(RNSPath path, ApplicationContext appContext, UIContext uiContext)
	{
		super(new GridBagLayout());
		
		_appContext = appContext;
		_uiContext = uiContext;
		_targetPath = path;
		
		add(SimplePanel.createVerticalPanel(
			GridBagConstraints.CENTER, GridBagConstraints.BOTH,
			GUIUtils.addTitle("Read Permissions", new JScrollPane(_readList)),
			GUIUtils.addTitle("Write Permissions", new JScrollPane(_writeList)),
			GUIUtils.addTitle("Execute Permissions", new JScrollPane(_executeList))),
			new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(5, 5, 5, 5), 5, 5));
		add(SimplePanel.createVerticalPanel(
			GridBagConstraints.CENTER, GridBagConstraints.NONE,
			GUIUtils.addTitle("Username/Password Token", 
				new UsernamePasswordComponent(_uiContext)),
			new PatternComponent(_uiContext),
			new EveryoneComponent(_uiContext)),
			new GridBagConstraints(0, 0, 1, 1, 0.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH,
				new Insets(5, 5, 5, 5), 5, 5));
	}
	
	ACLPanel(UIPluginContext context, RNSPath path)
	{
		this(path, context.applicationContext(), context.uiContext());
	}

	RNSPath targetPath()
	{
		return _targetPath;
	}
	
	public ACLPanel clone(ApplicationContext appContext, UIContext uiContext)
	{
		ACLPanel panel = new ACLPanel(
			_targetPath, appContext, uiContext);
		panel.load();
		
		return panel;
	}
	
	@Override
	public void load()
	{
		_uiContext.progressMonitorFactory().createMonitor(this, 
			"Retrieving ACL Information", "Retrieving ACLs", 1000L, 
			new ACLGetTask(), new AuthZConfigReceiver()).start();
	}
	
	public TearoffHandler createTearoffHandler()
	{
		return new TearoffHandlerImpl();
	}
	
	private Collection<AclEntry> findTargetList(Acl acl, ACLList target)
	{
		if (target == _readList)
			return acl.readAcl;
		else if (target == _writeList)
			return acl.writeAcl;
		else
			return acl.executeAcl;
	}
	
	private void update(Acl acl)
	{
		_readList.setEnabled(false);
		_writeList.setEnabled(false);
		_executeList.setEnabled(false);
		
		_uiContext.progressMonitorFactory().createMonitor(this, "Updating ACLs",
			"Updating ACLs", 1000L, new ACLSetTask(acl), 
			new AuthZConfigReceiver()).start();
	}
	
	private void add(ACLList target,
		Collection<ACLEntryWrapper> wrappers)
	{
		boolean changed = false;
		Acl acl = (Acl)_acl.clone();
		Collection<AclEntry> targetList = findTargetList(acl, target);
		
		for (ACLEntryWrapper wrapper : wrappers)
		{
			if (target.contains(wrapper))
				continue;
			
			targetList.add(wrapper.entry());
			changed = true;
		}
		
		if (changed)
			update(acl);
	}
	
	private void addRNS(ACLList target,
		Collection<Pair<RNSTreeNode, RNSPath>> paths)
	{
		Collection<RNSPath> targets = new Vector<RNSPath>(paths.size());
		for (Pair<RNSTreeNode, RNSPath> path : paths)
			targets.add(path.second());
		
		_uiContext.progressMonitorFactory().createMonitor(
			this, "Read Grid Identities", "Read grid identities.", 1000L,
			new RemoteIdentityLookupTask(targets), 
			new RemoteIdentityCompletionListener(target)).start();
	}
	
	private X509Identity readIdentity(File file)
		throws CertificateException, FileNotFoundException
	{
		InputStream in = null;
		
		try
		{
			in = new FileInputStream(file);
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate cert = (X509Certificate)cf.generateCertificate(in);
			return new X509Identity(new X509Certificate[] { cert } );
		}
		finally
		{
			StreamUtils.close(in);
		}
	}
	
	private Collection<X509Identity> formIdentities(List<File> files)
	{
		Collection<X509Identity> identities = new Vector<X509Identity>(
			files.size());
		for (File file : files)
		{
			try
			{
				identities.add(readIdentity(file));
			}
			catch (Throwable cause)
			{
				JOptionPane.showMessageDialog(this, String.format(
					"Unable to load certificate from file \"%s\".", file),
					"Certificate read error", JOptionPane.ERROR_MESSAGE);
				return null;
			}
		}
		
		return identities;
	}
	
	private void addLocal(ACLList target,
		List<File> files)
	{
		boolean changed = false;
		Acl acl = (Acl)_acl.clone();
		Collection<AclEntry> targetList = findTargetList(acl, target);
		
		Collection<X509Identity> identities = formIdentities(files);
		if (identities != null)
		{
			for (X509Identity identity : formIdentities(files))
			{
				targetList.add(identity);
				changed = true;
			}
		}
		
		if (changed)
			update(acl);
	}
	
	private void remove(ACLList target,
		Collection<ACLEntryWrapper> wrappers)
	{
		Acl acl = (Acl)_acl.clone();
		boolean changed = false;
		Collection<AclEntry> targetList = findTargetList(acl, target);
		
		for (ACLEntryWrapper wrapper : wrappers)
		{
			targetList.remove(wrapper.entry());
			changed = true;
		}
		
		if (changed)
			update(acl);
	}
	
	private void move(ACLList source, ACLList target,
		Collection<ACLEntryWrapper> wrappers)
	{
		boolean changed = false;
		Acl acl = (Acl)_acl.clone();
		Collection<AclEntry> targetList = findTargetList(acl, target);
		Collection<AclEntry> sourceList = findTargetList(acl, source);
		
		for (ACLEntryWrapper wrapper : wrappers)
		{
			changed = true;
			
			sourceList.remove(wrapper.entry());
			
			if (!target.contains(wrapper))
				targetList.add(wrapper.entry());
		}
		
		if (changed)
			update(acl);
	}
	
	private class RemoteIdentityCompletionListener
		implements TaskCompletionListener<X509Identity[]>
	{
		private ACLList _list;
		
		private RemoteIdentityCompletionListener(ACLList list)
		{
			_list = list;
		}
		
		@Override
		public void taskCancelled(Task<X509Identity[]> task)
		{
			// Do nothing
		}

		@Override
		public void taskCompleted(Task<X509Identity[]> task,
				X509Identity[] result)
		{
			Collection<ACLEntryWrapper> wrappers = new Vector<ACLEntryWrapper>(
				result.length);
			
			for (X509Identity id : result)
				wrappers.add(new ACLEntryWrapper(_uiContext, id));
			
			add(_list, wrappers);
		}

		@Override
		public void taskExcepted(Task<X509Identity[]> task, Throwable cause)
		{
			ErrorHandler.handleError(_uiContext, ACLPanel.this, cause);
		}
	}
	
	private class TearoffHandlerImpl implements TearoffHandler
	{
		@Override
		public Window tearoff(JComponent original)
		{
			UIContext uiContext = (UIContext)_uiContext.clone();
			
			return new ACLTearoffWindow(_appContext,
				uiContext, ACLPanel.this.clone(_appContext, uiContext));
		}
	}
	
	private class ACLGetTask extends AbstractTask<AuthZConfig>
	{
		@Override
		public AuthZConfig execute(TaskProgressListener progressListener)
				throws Exception
		{
			GenesisIIBaseRP rp = 
				(GenesisIIBaseRP)ResourcePropertyManager.createRPInterface(
					_uiContext.callingContext(), _targetPath.getEndpoint(), 
					GenesisIIBaseRP.class);
			AuthZConfig config = rp.getAuthZConfig();
			if (config == null)
				config = AclAuthZClientTool.getEmptyAuthZConfig();

			return config;
		}
	}
	
	private class AuthZConfigReceiver implements TaskCompletionListener<AuthZConfig>
	{
		@Override
		public void taskCancelled(Task<AuthZConfig> task)
		{
			_readList.cancel();
			_writeList.cancel();
			_executeList.cancel();
		}

		@Override
		public void taskCompleted(Task<AuthZConfig> task, AuthZConfig result)
		{
			try
			{
				_acl = Acl.decodeAcl(result);
				_readList.set(_uiContext, _acl.readAcl);
				_writeList.set(_uiContext, _acl.writeAcl);
				_executeList.set(_uiContext, _acl.executeAcl);
				
				ACLListTransferHandler transferHandler =
					new ACLListTransferHandler();
				
				_readList.setDragEnabled(true);
				_readList.setDropMode(DropMode.INSERT);
				_readList.setTransferHandler(transferHandler);
				
				_writeList.setDragEnabled(true);
				_writeList.setDropMode(DropMode.INSERT);
				_writeList.setTransferHandler(transferHandler);
				
				_executeList.setDragEnabled(true);
				_executeList.setDropMode(DropMode.INSERT);
				_executeList.setTransferHandler(transferHandler);
			}
			catch (Throwable cause)
			{
				taskExcepted(task, cause);
			}
		}

		@Override
		public void taskExcepted(Task<AuthZConfig> task, Throwable cause)
		{
			_readList.error();
			_writeList.error();
			_executeList.error();
			
			ErrorHandler.handleError(_uiContext, ACLPanel.this, cause);
		}
	}
	
	private class ACLSetTask extends AbstractTask<AuthZConfig>
	{
		private Acl _newACL;
		
		private ACLSetTask(Acl newACL)
		{
			_newACL = newACL;
		}
		
		@Override
		public AuthZConfig execute(TaskProgressListener progressListener)
				throws Exception
		{
			GenesisIIBaseRP rp = 
				(GenesisIIBaseRP)ResourcePropertyManager.createRPInterface(
					_uiContext.callingContext(), _targetPath.getEndpoint(), 
					GenesisIIBaseRP.class);
			AuthZConfig config = Acl.encodeAcl(_newACL);
			rp.setAuthZConfig(config);
			return config;
		}
	}
	
	private class ACLListTransferHandler extends TransferHandler
	{
		static final long serialVersionUID = 0L;
		
		@Override
		public boolean canImport(TransferSupport support)
		{
			try
			{
				if (!support.getComponent().isEnabled())
					return false;
				
				if (support.isDataFlavorSupported(ACLTransferable.DATA_FLAVOR))
				{
					if (!support.isDrop())
						return true;
					
					ACLEntryWrapperTransferData data =
						(ACLEntryWrapperTransferData)support.getTransferable(
							).getTransferData(ACLTransferable.DATA_FLAVOR);
					if (data.source() == support.getComponent())
						return false;
					
					if ((support.getSourceDropActions() & LINK) > 0x0)
						support.setDropAction(LINK);
					if ((support.getSourceDropActions() & COPY) > 0x0)
						support.setDropAction(COPY);
					
					return true;
				} else if (support.isDataFlavorSupported(
					RNSListTransferable.RNS_PATH_LIST_FLAVOR))
				{
					if (!support.isDrop())
						return true;
					
					support.setDropAction(LINK);
					return true;
				} else if (
					support.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
				{
					if (!support.isDrop())
						return true;
					
					support.setDropAction(LINK);
					return true;
				}
			}
			catch (Throwable cause)
			{
				_logger.warn("Unable to check for DnD capability.", cause);
			}
			
			return false;
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean importData(TransferSupport support)
		{
			try
			{
				if (support.isDataFlavorSupported(ACLTransferable.DATA_FLAVOR))
				{
					ACLEntryWrapperTransferData data =
						(ACLEntryWrapperTransferData)support.getTransferable(
							).getTransferData(ACLTransferable.DATA_FLAVOR);
					
					if (!support.isDrop())
					{
						add((ACLList)support.getComponent(), data.wrappers());
					} else if (support.getDropAction() == MOVE && data.source() != null)
					{
						move(data.source(), (ACLList)support.getComponent(),
							data.wrappers());
						data.handled(true);
					} else
						add((ACLList)support.getComponent(), data.wrappers());
					
					return true;
				} else if (support.isDataFlavorSupported(
					RNSListTransferable.RNS_PATH_LIST_FLAVOR))
				{
					
					if (support.isDrop() && support.getDropAction() != LINK)
						return false;
					
					RNSListTransferData data =
						(RNSListTransferData)support.getTransferable(
							).getTransferData(
								RNSListTransferable.RNS_PATH_LIST_FLAVOR);
					addRNS((ACLList)support.getComponent(), data.paths());
					
					return true;
				} else if (
					support.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
				{
					List<File> files = (List<File>)support.getTransferable(
						).getTransferData(DataFlavor.javaFileListFlavor);
					if (files != null)
					{
						for (File f : files)
						{
							if (!f.isFile())
								return false;
						}
						
						addLocal((ACLList)support.getComponent(), files);
					}
					
					return true;
				}
			}
			catch (Throwable cause)
			{
				_logger.warn("Unable to import data.", cause);
			}
			
			return false;
		}

		@Override
		protected Transferable createTransferable(JComponent c)
		{
			return ((ACLList)c).createTransferable();
		}

		@Override
		protected void exportDone(JComponent source, Transferable tData,
			int action)
		{
			try
			{
				if (tData.isDataFlavorSupported(ACLTransferable.DATA_FLAVOR))
				{
					ACLEntryWrapperTransferData data =
						(ACLEntryWrapperTransferData)tData.getTransferData(
							ACLTransferable.DATA_FLAVOR);
					
					if (action == MOVE && !data.handled())
						remove(data.source(), data.wrappers());
				}
			}
			catch (Throwable cause)
			{
				_logger.warn("Unable to finish export of data.", cause);
			}
		}

		@Override
		public int getSourceActions(JComponent c)
		{
			return COPY_OR_MOVE;
		}
	}
	
	class DeleteAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;
		
		private ACLList _list = null;
		
		private DeleteAction()
		{
			super("Delete");
		}
		
		void setACLList(ACLList list)
		{
			_list = list;
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			try
			{
				ACLTransferable transferable =
					(ACLTransferable)_list.createTransferable();
				if (transferable == null)
					return;
				
				ACLEntryWrapperTransferData data = 
					(ACLEntryWrapperTransferData)transferable.getTransferData(
						ACLTransferable.DATA_FLAVOR);
				if (data == null)
					return;
				
				Collection<ACLEntryWrapper> wrappers = data.wrappers();
				if (wrappers == null || wrappers.size() == 0)
					return;
				
				int answer = JOptionPane.showConfirmDialog(_list,
					"Are you sure you want to delete the selected entries?", 
					"Delete Confirmation", JOptionPane.YES_NO_OPTION);
				if (answer == JOptionPane.YES_OPTION)
					remove(_list, wrappers);
			}
			catch (Throwable cause)
			{
				_logger.warn("Error trying to delete ACL list entries.",
					cause);
			}
		}
	}
}