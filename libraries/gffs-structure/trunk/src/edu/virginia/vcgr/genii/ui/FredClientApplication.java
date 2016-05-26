package edu.virginia.vcgr.genii.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.prefs.BackingStoreException;

import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.utils.gui.tearoff.TearoffPanel;

import edu.virginia.vcgr.genii.client.gui.GuiHelpAction;
import edu.virginia.vcgr.genii.client.gui.HelpLinkConfiguration;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.ui.LoggingLinkage.LoggingListModel;
import edu.virginia.vcgr.genii.ui.login.CredentialManagementButton;
import edu.virginia.vcgr.genii.ui.plugins.EndpointDescription;
import edu.virginia.vcgr.genii.ui.plugins.LazilyLoadedTab;
import edu.virginia.vcgr.genii.ui.plugins.UIPluginContext;
import edu.virginia.vcgr.genii.ui.plugins.UIPlugins;
import edu.virginia.vcgr.genii.ui.plugins.shell.GridShellPlugin;
import edu.virginia.vcgr.genii.ui.rns.RNSFilledInTreeObject;
import edu.virginia.vcgr.genii.ui.rns.RNSTreeModel;
import edu.virginia.vcgr.genii.ui.rns.RNSTreeModel.ShowWhichTypes;
import edu.virginia.vcgr.genii.ui.rns.RNSTreeNode;
import edu.virginia.vcgr.genii.ui.rns.RNSTreeObject;
import edu.virginia.vcgr.genii.ui.rns.RNSTreeObjectType;
import edu.virginia.vcgr.genii.ui.rns.RNSTree;
import edu.virginia.vcgr.genii.ui.trash.TrashCanWidget;

@SuppressWarnings("rawtypes")
public class FredClientApplication extends UIFrame
{
	static final long serialVersionUID = 0L;
	static private Log _logger = LogFactory.getLog(FredClientApplication.class);

	static final private Dimension TABBED_PANE_SIZE = new Dimension(400, 200);
	// hmmm: make this size rational.

	private Object _joinLock = new Object();

	// set to true if the full application should close.
	private boolean _exit = false;

	private RNSTree _browserTree;
	private RNSTree _fileList;
	private JTabbedPane _tabbedPane = new JTabbedPane();
	private JList _debugTarget; // text area that is targeted for informative updates.
	private LoggingLinkage _debugLinkage; // connects our debugging target to logging.
	private JSplitPane _browserPane; // the splitter with the dirs on left and files on right.
	private JScrollPane _fileScroller; // the scroller that holds our file browser.
	private UIPlugins _filePlugins; //

	private ArrayList<String> _activities = new ArrayList<String>(); // queued actions for UI to take.

	public FredClientApplication(boolean launchShell) throws FileNotFoundException, RNSPathDoesNotExistException, IOException
	{
		this(new UIContext(new ApplicationContext()), launchShell);
	}

	public FredClientApplication(UIContext context, boolean launchShell)
		throws FileNotFoundException, IOException, RNSPathDoesNotExistException
	{
		this(context, launchShell, null);
	}

	@SuppressWarnings("unchecked")
	public FredClientApplication(UIContext context, boolean launchShell, String startPath)
		throws FileNotFoundException, IOException, RNSPathDoesNotExistException
	{
		super(context, "XSEDE GFFS Browser");
		// write a brag in the log file.
		_logger.info("XSEDE GFFS Client GUI Is Provided as part of Genesis II from the University of Virginia.");

		if (!getUIContext().applicationContext().isInitialized()) {
			getUIContext().applicationContext().setApplicationEventListener(new ApplicationEventListenerImpl());

			if (getUIContext().applicationContext().isMacOS())
				setupMacApplication();
		}

		// _tabbedPane.setMinimumSize(TABBED_PANE_SIZE);
		_tabbedPane.setPreferredSize(TABBED_PANE_SIZE);

		_tabbedPane.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				_logger.debug("got into the state changed for tabbed pane");

				Component jc = _tabbedPane.getSelectedComponent();
				if (jc != null && (jc instanceof LazilyLoadedTab))
					((LazilyLoadedTab) jc).load();
			}
		});

		// set up the browser tree on the left side.
		_browserTree = new RNSTree(getUIContext().applicationContext(), _uiContext, startPath, ShowWhichTypes.JUST_DIRECTORIES);
		JScrollPane dirScroller = new JScrollPane(_browserTree);
		// scroller.setMinimumSize(RNSTree.DESIRED_BROWSER_SIZE);
		dirScroller.setPreferredSize(RNSTree.DESIRED_BROWSER_SIZE);

		Container outerPane = getContentPane();

		// create the browser splitter, where we'll have dirs on left and files on right.
		_browserPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
		// set the directory viewer in the pane.
		_browserPane.setLeftComponent(dirScroller);
		// GUIUtils.addTitle("GFFS Directories",
		// new TearoffPanel(dirScroller, _browserTree.createTearoffHandler(dirPlugins.getContext()), new IconBasedTearoffThumb()));
		// );

		_fileList = new RNSTree(getUIContext().applicationContext(), _uiContext, startPath, ShowWhichTypes.DIRECTORIES_AND_FILES);

		_fileScroller = new JScrollPane(_fileList);
		_fileScroller.setPreferredSize(RNSTree.DESIRED_BROWSER_SIZE);

		// create the plugins object for managing the file list.
		_filePlugins = new UIPlugins(new UIPluginContext(_uiContext, _fileList, _fileList));

		// hook in the menus.
		_filePlugins.addTopLevelMenus(getJMenuBar());

		/*
		 * this must come second since the first call instantiates a bunch of static things hooked to the "real" viewer (the file browser on
		 * the right side).
		 */
		// not needed? UIPlugins dirPlugins = new UIPlugins(new UIPluginContext(_uiContext, _fileList, _browserTree));
		// dirPlugins.addTopLevelMenus(browMenu);

		// set the file viewer in the right side of the pane.
		_browserPane.setRightComponent(
			// GUIUtils.addTitle("Browser",
			new TearoffPanel(_fileScroller, _fileList.createTearoffHandler(getUIContext().applicationContext()),
				new IconBasedTearoffThumb()));

		// now set up the lower parts of the panel, where the credentials button and trash can are, with the diagnostics below them.
		Insets zeroInset = new Insets(0, 0, 0, 0);

		JPanel utilsPanel = new JPanel(new GridBagLayout());
		utilsPanel.add(new CredentialManagementButton(_uiContext),
			new GridBagConstraints(0, 0, 1, 1, 1.0, 0.3, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 5, 0, 0), 0, 0));

		utilsPanel.add(new TrashCanWidget(getUIContext().applicationContext(), _uiContext),
			new GridBagConstraints(1, 0, 1, 1, 1.0, 0.3, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));

		// add a diagnostics logging view.
		LoggingListModel listModel = new LoggingListModel();
		_debugTarget = new JList(listModel);
		JScrollPane debugScroller = new JScrollPane(_debugTarget);
		utilsPanel.add(debugScroller,
			new GridBagConstraints(0, 1, 3, 1, 1.0, 0.7, GridBagConstraints.CENTER, GridBagConstraints.BOTH, zeroInset, 0, 0));

		// make the lower split pane first...
		JSplitPane utilsAndTabbedPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
		utilsAndTabbedPanel.setTopComponent(_tabbedPane);
		utilsAndTabbedPanel.setBottomComponent(utilsPanel);

		// create the splitter for the browser on top and tabs on bottom.
		JSplitPane uberSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
		uberSplitter.setTopComponent(_browserPane);
		uberSplitter.setBottomComponent(utilsAndTabbedPanel);

		outerPane.add(uberSplitter,
			new GridBagConstraints(0, 0, 3, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, zeroInset, 0, 0));

		_fileList.addTreeSelectionListener(new RNSFileSelectionListener(_filePlugins));
		_fileList.addMouseListener(new RNSTreePopupListener(_filePlugins));

		_browserTree.addTreeSelectionListener(new RNSDirectorySelectionListener());
		// cross usage below
		_browserTree.addMouseListener(new RNSTreePopupListener(_filePlugins));

		getMenuFactory().addHelpMenu(getUIContext(), getJMenuBar());
		// not needed? UIPluginContext.getMenuFactory(dirPlugins.getContext().uiContext()).addHelpMenu(dirPlugins.getContext(),
		// getJMenuBar());

		if (launchShell) {
			_filePlugins.fireMenuAction(GridShellPlugin.class);
		}

		_debugLinkage = new LoggingLinkage(_debugTarget);
		_debugLinkage.consumeLogging("Application Started", new Exception("All is well."));

		/*
		 * try { EndpointReferenceType epr = new LocalContainer(_uiContext).getEndpoint(); EnhancedRNSPortType rns =
		 * ClientUtils.createProxy(EnhancedRNSPortType.class, epr, _uiContext.callingContext()); ListResponse resp = rns.list(new List()); for
		 * (EntryType entry : resp.getEntryList()) { System.err.format("Entry:  %s\n", entry.getEntry_name()); } } catch
		 * (ContainerNotRunningException cnre) { _logger.info("exception in ClientApplication", cnre); }
		 */
	}

	public static String SELECT_FILEBROWSER_TOP = "seltop";

	/**
	 * adds a new activity for the UI to take once the main thread calls the pulseActivity method.
	 */
	public void addActivity(String toAdd)
	{
		_activities.add(toAdd);
	}

	/**
	 * called by the main thread periodically to get pending activities done.
	 */
	public void pulseActivities()
	{
		// swap out any pending activities with an empty list.
		ArrayList<String> pending = _activities;
		_activities = new ArrayList<String>();
		// process the pending activities.
		for (String activity : pending) {
			if (activity.equals(SELECT_FILEBROWSER_TOP)) {
				// update to select the file list's first element.
				_fileList.setSelectionPath(_fileList.getPathForRow(0));
			}
		}
	}

	/**
	 * puts a new file browser pointing at the "path" into the right pane.
	 */
	public void reloadFileBrowser(String path)
	{
		RNSTreeModel oldModel = (RNSTreeModel) _fileList.getModel();
		RNSFilledInTreeObject userObj = (RNSFilledInTreeObject) oldModel.treeTop().getUserObject();
		String topPath = userObj.path().toString();
		_logger.debug("got a current top-path of " + topPath);
		if (topPath.equals(path)) {
			// we're already there.
			return;
		}

		try {
			RNSTreeModel newModel =
				new RNSTreeModel(getUIContext().applicationContext(), _uiContext, path, ShowWhichTypes.DIRECTORIES_AND_FILES);

			_fileList.remodel(newModel);

			// do we really need these??
			// _fileList.addMouseListener(new RNSTreePopupListener(_filePlugins));
			// _fileList.addTreeSelectionListener(new RNSFileSelectionListener(_filePlugins));

		} catch (RNSPathDoesNotExistException e) {
			_logger.error("failure to repoint the file browser at path '" + path + "'");
		}
	}

	@Override
	public void dispose()
	{
		if (handleQuit()) {
			getUIContext().applicationContext().fireDispose();
		}
	}

	private void setupMacApplication()
	{
		MacOSXSpecifics.setupMacOSApplication(getUIContext().applicationContext());
	}

	/*
	 * shows a new status line in the message window at the bottom of the UI.
	 */
	public void addStatusLine(String message, String detail)
	{
		_debugLinkage.consumeLogging(message, new Exception(detail));
	}

	/*
	 * add a status line without extra detail.
	 */
	public void addStatusLine(String message)
	{
		addStatusLine(message, "status message");
	}

	protected boolean handleQuit()
	{
		if (!getUIContext().applicationContext().fireQuitRequested()) {
			return false;
		}
		synchronized (_joinLock) {
			_exit = true;
			_joinLock.notifyAll();
		}
		return true;
	}

	public void join() throws InterruptedException
	{
		synchronized (_joinLock) {
			while (!_exit)
				_joinLock.wait();
		}
	}

	public UIContext getContext()
	{
		return _uiContext;
	}

	private class ApplicationEventListenerImpl implements ApplicationEventListener
	{
		@Override
		public void aboutRequested()
		{
			GuiHelpAction.DisplayUrlHelp(HelpLinkConfiguration.get_help_url(HelpLinkConfiguration.MAIN_HELP));
		}

		@Override
		public void preferencesRequested()
		{
			try {
				_uiContext.preferences().launchEditor(FredClientApplication.this);
			} catch (BackingStoreException bse) {
				JOptionPane.showMessageDialog(FredClientApplication.this, "Unable to store preferences.", "Preferences Store Exception",
					JOptionPane.ERROR_MESSAGE);
			}
		}

		@Override
		public boolean quitRequested()
		{
			boolean ret = handleQuit();
			if (ret) {
				getUIContext().applicationContext().fireDispose();
			}
			return ret;
		}
	}

	private class RNSDirectorySelectionListener implements TreeSelectionListener
	{
		private RNSDirectorySelectionListener()
		{
		}

		@Override
		public void valueChanged(TreeSelectionEvent e)
		{
			TreePath[] paths = _browserTree.getSelectionPaths();
			if (paths != null) {
				_logger.debug("into tree handler for rns dirs: " + e);
				for (TreePath path : paths) {
					RNSTreeNode node = (RNSTreeNode) path.getLastPathComponent();
					RNSTreeObject obj = (RNSTreeObject) node.getUserObject();
					if (obj.objectType() == RNSTreeObjectType.ENDPOINT_OBJECT) {
						RNSFilledInTreeObject fObj = (RNSFilledInTreeObject) obj;
						if (_logger.isDebugEnabled())
							_logger
								.debug("hit tree node value changed at '" + fObj.path().toString() + "' for dir listener: " + e.toString());
						reloadFileBrowser(fObj.path().toString());

						// schedule an update of the file browser.
						addActivity(SELECT_FILEBROWSER_TOP);
					}
				}
			}
		}
	}

	private class RNSFileSelectionListener implements TreeSelectionListener
	{
		private UIPlugins _plugins;

		private RNSFileSelectionListener(UIPlugins plugins)
		{
			_plugins = plugins;
		}

		@Override
		public void valueChanged(TreeSelectionEvent e)
		{
			Collection<EndpointDescription> descriptions = new LinkedList<EndpointDescription>();
			TreePath[] paths = _fileList.getSelectionPaths();
			if (paths != null) {
				_logger.debug("into tree handler for file viewer window: " + e);

				for (TreePath path : paths) {
					RNSTreeNode node = (RNSTreeNode) path.getLastPathComponent();
					RNSTreeObject obj = (RNSTreeObject) node.getUserObject();
					if (obj.objectType() == RNSTreeObjectType.ENDPOINT_OBJECT) {
						RNSFilledInTreeObject fObj = (RNSFilledInTreeObject) obj;

						_logger.info("got to the file selection listener on path " + fObj.path());

						descriptions.add(new EndpointDescription(fObj.typeInformation(), fObj.endpointType(), fObj.isLocal()));
					}
				}
			}

			_plugins.updateStatuses(descriptions);
			_plugins.setTabPanes(_tabbedPane, descriptions);
		}
	}

}
