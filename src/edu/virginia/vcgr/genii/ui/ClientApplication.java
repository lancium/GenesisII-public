package edu.virginia.vcgr.genii.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.prefs.BackingStoreException;

import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import org.morgan.utils.gui.GUIUtils;
import org.morgan.utils.gui.tearoff.TearoffPanel;

import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.ui.LoggingLinkage.LoggingListModel;
import edu.virginia.vcgr.genii.ui.login.CredentialManagementButton;
import edu.virginia.vcgr.genii.ui.plugins.EndpointDescription;
import edu.virginia.vcgr.genii.ui.plugins.LazilyLoadedTab;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginContext;
import edu.virginia.vcgr.genii.ui.plugins.UIPlugins;
import edu.virginia.vcgr.genii.ui.plugins.shell.GridShellPlugin;
import edu.virginia.vcgr.genii.ui.rns.RNSFilledInTreeObject;
import edu.virginia.vcgr.genii.ui.rns.RNSTree;
import edu.virginia.vcgr.genii.ui.rns.RNSTreeNode;
import edu.virginia.vcgr.genii.ui.rns.RNSTreeObject;
import edu.virginia.vcgr.genii.ui.rns.RNSTreeObjectType;
import edu.virginia.vcgr.genii.ui.trash.TrashCanWidget;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ClientApplication extends UIFrame
{
	static final long serialVersionUID = 0L;

	static private Log _logger = LogFactory.getLog(ClientApplication.class);

	static final private Dimension TABBED_PANE_SIZE =
		new Dimension(700, 500);
	
	private Object _joinLock = new Object();
	private boolean _exit = false;
	private RNSTree _browserTree;
	private JTabbedPane _tabbedPane = new JTabbedPane();	
	private JList _debugTarget;  // text area that is targeted for informative updates.
	private LoggingLinkage _debugLinkage;  // connects our debugging target to logging. 
	
	private void setupMacApplication()
	{
		MacOSXSpecifics.setupMacOSApplication(_context);
	}
	
	protected boolean handleQuit()
	{
		if (!_context.fireQuitRequested())
			return false;
		
		synchronized(_joinLock)
		{
			_exit = true;
			_joinLock.notifyAll();
		}
		
		return true;
	}
	
	public ClientApplication(boolean launchShell)
		throws FileNotFoundException, RNSPathDoesNotExistException, IOException
	{
		this(new UIContext(new ApplicationContext()), launchShell);
	}
	
	public ClientApplication(UIContext context, boolean launchShell)
		throws FileNotFoundException, IOException, RNSPathDoesNotExistException
	{
		super(context,
			"Genesis II Client Application");
		
		if (!_context.isInitialized())
		{
			_context.setApplicationEventListener(
				new ApplicationEventListenerImpl());
			
			if (_context.isMacOS())
				setupMacApplication();
		}
		
		_tabbedPane.setMinimumSize(TABBED_PANE_SIZE);
		_tabbedPane.setPreferredSize(TABBED_PANE_SIZE);
		
		_tabbedPane.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				Component jc = _tabbedPane.getSelectedComponent();
				if (jc != null && (jc instanceof LazilyLoadedTab))
					((LazilyLoadedTab)jc).load();
			}
		});

		_browserTree = new RNSTree(_context, _uiContext);
		JScrollPane scroller = new JScrollPane(_browserTree);
		scroller.setMinimumSize(RNSTree.DESIRED_BROWSER_SIZE);
		scroller.setPreferredSize(RNSTree.DESIRED_BROWSER_SIZE);
		
		Container content = getContentPane();
		
		JSplitPane splitPane = new JSplitPane(
			JSplitPane.HORIZONTAL_SPLIT, true);
		splitPane.setLeftComponent(GUIUtils.addTitle("RNS Space", 
			new TearoffPanel(scroller,
				_browserTree.createTearoffHandler(_context), 
				new IconBasedTearoffThumb())));
		splitPane.setRightComponent(_tabbedPane);
		
		content.add(splitPane,
			new GridBagConstraints(0, 0, 3, 1, 1.0, 1.0, 
				GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));

		content.add(new CredentialManagementButton(_uiContext), 
			new GridBagConstraints(
				0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST,
				GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
				
                content.add(new TrashCanWidget(_context, _uiContext),
                        new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0,
                            GridBagConstraints.EAST, GridBagConstraints.NONE,
                            new Insets(5, 5, 5, 5), 5, 5));
            
		// new code to add a diagnostics logging view.
		LoggingListModel listModel = new LoggingListModel();
		_debugTarget = new JList(listModel);
		JScrollPane debugScroller = new JScrollPane(_debugTarget);
		content.add(debugScroller, new GridBagConstraints(
				0, 2, 3, 1, 1.0, 1.0, GridBagConstraints.CENTER,
				GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));		
		
		UIPlugins plugins = new UIPlugins(
			new UIPluginContext(_uiContext, _browserTree, _browserTree));
		plugins.addTopLevelMenus(getJMenuBar());
		_browserTree.addTreeSelectionListener(new RNSSelectionListener(plugins));
		
		getMenuFactory().addHelpMenu(_uiContext, getJMenuBar());
		
		_browserTree.addMouseListener(new RNSTreePopupListener(plugins));
		
		if (launchShell)
			plugins.fireMenuAction(GridShellPlugin.class);
		
		_debugLinkage = new LoggingLinkage(_debugTarget);
		_debugLinkage.consumeLogging("Application Started", new Exception("All is well."));
		
		/*
		try
		{
			EndpointReferenceType epr = new LocalContainer(_uiContext).getEndpoint();
			EnhancedRNSPortType rns = ClientUtils.createProxy(EnhancedRNSPortType.class, epr, _uiContext.callingContext());
			ListResponse resp = rns.list(new List());
			for (EntryType entry : resp.getEntryList())
			{
				System.err.format("Entry:  %s\n", entry.getEntry_name());
			}
		}
		catch (ContainerNotRunningException cnre)
		{
			_logger.info("exception in ClientApplication", cnre);
		}
		*/
	}
	
	@Override
	public void dispose()
	{
		if (handleQuit())
			_context.fireDispose();
	}
	
	public void join() throws InterruptedException
	{
		synchronized(_joinLock)
		{
			while (!_exit)
				_joinLock.wait();
		}
	}
	
	public UIContext getContext() {
	    return _uiContext;
	}
	
	private class ApplicationEventListenerImpl implements ApplicationEventListener
	{
		@Override
		public void aboutRequested()
		{
			System.err.println("About not implemented.");
		}

		@Override
		public void preferencesRequested()
		{
			try
			{
				_uiContext.preferences().launchEditor(
					ClientApplication.this);
			}
			catch (BackingStoreException bse)
			{
				JOptionPane.showMessageDialog(
					ClientApplication.this, "Unable to store preferences.",
					"Preferences Store Exception", JOptionPane.ERROR_MESSAGE);
			}
		}

		@Override
		public boolean quitRequested()
		{
			boolean ret = handleQuit();
			if (ret)
				_context.fireDispose();
			return ret;
		}
	}
	
	private class RNSSelectionListener implements TreeSelectionListener
	{
		private UIPlugins _plugins;
		
		private RNSSelectionListener(UIPlugins plugins)
		{
			_plugins = plugins;
		}

		@Override
		public void valueChanged(TreeSelectionEvent e)
		{
			Collection<EndpointDescription> descriptions =
				new LinkedList<EndpointDescription>();
			TreePath []paths = _browserTree.getSelectionPaths();
			if (paths != null)
			{
				for (TreePath path : paths)
				{
					RNSTreeNode node = (RNSTreeNode)path.getLastPathComponent();
					RNSTreeObject obj = (RNSTreeObject)node.getUserObject();
					if (obj.objectType() == RNSTreeObjectType.ENDPOINT_OBJECT)
					{
						RNSFilledInTreeObject fObj = (RNSFilledInTreeObject)obj;
						descriptions.add(new EndpointDescription(fObj.typeInformation(),
							fObj.endpointType(), fObj.isLocal()));
					}
				}
			}
			
			_plugins.updateStatuses(descriptions);
			_plugins.setTabPanes(_tabbedPane, descriptions);
		}
	}
}
