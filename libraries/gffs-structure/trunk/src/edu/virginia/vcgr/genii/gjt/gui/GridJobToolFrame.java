package edu.virginia.vcgr.genii.gjt.gui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import edu.virginia.vcgr.genii.gjt.JobDocumentContext;
import edu.virginia.vcgr.genii.gjt.data.JobDocument;
import edu.virginia.vcgr.genii.gjt.data.ModificationListener;
import edu.virginia.vcgr.genii.gjt.data.recent.Recents;
import edu.virginia.vcgr.genii.gjt.gui.basic.BasicJobInformation;
import edu.virginia.vcgr.genii.gjt.gui.fs.FilesystemMenu;
import edu.virginia.vcgr.genii.gjt.gui.icons.ShapeIcons;
import edu.virginia.vcgr.genii.gjt.gui.resource.ResourcesPanel;
import edu.virginia.vcgr.genii.gjt.gui.stage.DataTab;
import edu.virginia.vcgr.genii.gjt.gui.util.ButtonPanel;
import edu.virginia.vcgr.genii.gjt.gui.util.SimpleIconButton;
import edu.virginia.vcgr.genii.gjt.gui.variables.VariablePanel;
import edu.virginia.vcgr.genii.gjt.gui.variables.VariableTableModel;
import edu.virginia.vcgr.genii.ui.BasicFrameWindow;
import edu.virginia.vcgr.jsdl.OperatingSystemNames;

public abstract class GridJobToolFrame extends BasicFrameWindow
{
	static final long serialVersionUID = 0L;

	protected JobDocumentContext _documentContext;

	private JTabbedPane _superTabbedPane = new JTabbedPane();

	private List<JTabbedPane> _tabbedPane = new ArrayList<JTabbedPane>();

	private int _tabIndex;

	private GenerateJSDLAction _generateJSDL;

	private boolean _modified = false;

	private boolean _initial;

	private List<ErrorPanel> _ePanels = new ArrayList<ErrorPanel>();

	private VariableTableModel _varTableModel;

	private void addMenu(JMenuBar bar, JMenu menu)
	{
		if (bar != null && menu != null)
			bar.add(menu);
	}

	protected GridJobToolFrame(JobDocumentContext documentContext)
	{
		_generateJSDL = new GenerateJSDLAction(documentContext.applicationContext().willLaunch());

		_tabIndex = 0;// Used to keep track of the number of job description tabs that have been
						// added

		_documentContext = documentContext;

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		Container container = getContentPane();
		container.setLayout(new GridBagLayout());

		// Add the main tabbed pane to the GUI
		add(_superTabbedPane,
			new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));

		// if()
		_varTableModel = new VariableTableModel(documentContext, 0);

		/*
		 * _documentContext.getModificationBroker().addModificationListener(new ModificationListenerImpl()); ErrorPanel ePanel = new
		 * ErrorPanel(_generateJSDL, _documentContext.jobRoot().jobDocument().get(_tabIndex));
		 * _documentContext.getModificationBroker().addModificationListener(ePanel); _ePanels.add(ePanel);
		 * 
		 * 
		 * //if(size > 1){ // Add the tabs comprising the "Common" tab to the "inner" tabbed pane addTabs(documentContext, _tabIndex);
		 * 
		 * // Add the common tab to the "outer" tabbed pane _superTabbedPane.add("Job Description", _tabbedPane.get(0)); _tabIndex++; //}
		 * //else{
		 * 
		 * //}
		 */

		// Add the plus button to the GUI in its correct location
		createSuperTab(documentContext);

		ButtonPanel plusButton = ButtonPanel.createHorizontalPanel(new SimpleIconButton(ShapeIcons.Plus, new PlusAction()));
		add(plusButton, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL,
			new Insets(0, 0, 0, 0), 0, 0));

		ButtonPanel minusButton = ButtonPanel.createHorizontalPanel(new SimpleIconButton(ShapeIcons.Minus, new MinusAction()));
		add(minusButton, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHEAST, GridBagConstraints.HORIZONTAL,
			new Insets(0, 0, 0, 0), 0, 0));
		// minusButton.setEnabled(false);

		// _ePanels.add(null);// Common Tab should not have an error panel

		// Adds the first job description tab to the "outer" tabbed pane

		add(_ePanels.get(0), new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
			new Insets(5, 5, 5, 5), 5, 5));

		// Adds extra job description tabs. This occurs when the user loads
		// a .gjp file that was saved with more than one job description tab. This
		// step obviates the need for the user to click the "plus" button
		// on the GUI to make the other job description tabs visible.

		_superTabbedPane.addChangeListener(new TabbedPaneSelection());
		int size = _documentContext.jobRoot().jobDocument().size();
		System.out.println("Size of Job Documents: " + size);
		if (size > 2) {
			_ePanels.set(0, null);
			for (int i = 1; i < size; i++) {
				createSuperTab(documentContext);
				_superTabbedPane.setTitleAt(0, "common");
			}
			// _superTabbedPane.setSelectedIndex(1);
		}

		JMenuBar menuBar = createMenuBar(_generateJSDL);
		if (menuBar != null)
			setJMenuBar(menuBar);

		// hmmm: Remove this later (why? too big?)
		setPreferredSize(new Dimension(750, 750));

	}

	abstract protected Action getExitAction();

	abstract protected Action getPreferencesAction();

	abstract protected Action getAboutAction();

	final public boolean isModified()
	{
		return _modified;
	}

	final public boolean isInitial()
	{
		return _initial;
	}

	protected JMenu createRecentsMenu()
	{
		JMenu ret = new JMenu("Recents");
		ret.addMenuListener(new RecentsListener());

		return ret;
	}

	protected JMenu createFileMenu(AbstractAction generateJSDLAction)
	{
		JMenu menu = new JMenu("File");
		menu.setMnemonic('F');

		menu.add(new NewDocumentAction());
		menu.add(new OpenDocumentAction());
		menu.add(createRecentsMenu());
		menu.addSeparator();
		menu.add(new CloseDocumentAction());
		menu.addSeparator();
		menu.add(new SaveDocumentAction());
		menu.add(new SaveDocumentAsAction());
		menu.add(new SaveAllDocumentsAction());
		menu.addSeparator();
		menu.add(generateJSDLAction);
		GenerateJSDLAction action = (GenerateJSDLAction) generateJSDLAction;
		if ((action != null) && action.willLaunch()) {
			/*
			 * if the generate jsdl actor is "for real" and hooked to a queue or BES, then we'll add a menu for generating jsdl separately. we
			 * always want to be able to store the project as a jsdl file, but when hooked to a queue that menu item becomes "submit job"
			 * instead.
			 */
			menu.add(new GenerateJSDLAction(false));
		}

		Action preferencesAction = getPreferencesAction();
		if (preferencesAction != null) {
			menu.addSeparator();
			menu.add(preferencesAction);
		}

		Action exitAction = getExitAction();
		if (exitAction != null) {
			menu.addSeparator();
			menu.add(exitAction);
		}

		return menu;
	}

	protected JMenuBar createMenuBar(GenerateJSDLAction generateJSDLAction)
	{
		JMenuBar bar = new JMenuBar();
		addMenu(bar, createFileMenu(generateJSDLAction));
		addMenu(bar, new FilesystemMenu(_documentContext.jobRoot().jobDocument().get(0).filesystemMap()));

		Action aboutAction = getAboutAction();
		if (aboutAction != null) {
			JMenu helpMenu = new JMenu("Help");
			helpMenu.add(aboutAction);
			bar.add(Box.createHorizontalGlue());
			addMenu(bar, helpMenu);
		}

		return bar;
	}

	protected void addTabs(JobDocumentContext documentContext, int tabIndex)
	{
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.add(new BasicJobInformation(documentContext, tabIndex));
		tabbedPane.add(new DataTab(documentContext, tabIndex));
		tabbedPane.add(new ResourcesPanel(documentContext, tabIndex));

		VariablePanel vp = new VariablePanel(tabbedPane, documentContext, tabIndex, _varTableModel);
		tabbedPane.add(vp);
		tabbedPane.setEnabledAt(tabbedPane.indexOfComponent(vp), vp.isEnabled());

		_tabbedPane.add(tabbedPane);

	}

	protected void createSuperTab(JobDocumentContext documentContext)
	{
		// Add Modification Listener for the job variables

		_documentContext.getModificationBroker().addModificationListener(new ModificationListenerImpl());

		// Add modification listener for a particular job document's error panel
		ErrorPanel ePanel = new ErrorPanel(_generateJSDL, _documentContext.jobRoot().jobDocument().get(_tabIndex));
		_documentContext.getModificationBroker().addModificationListener(ePanel);
		_ePanels.add(ePanel);

		// Add the new job description tab to the "outer" tabbed pane
		addTabs(_documentContext, _tabIndex);
		/*
		 * if(_tabIndex > 0) _superTabbedPane.add("Job Description " + _tabIndex, _tabbedPane.get(_tabIndex)); else _superTabbedPane.add(
		 * "Job Description", _tabbedPane.get(_tabIndex));
		 */

		_superTabbedPane.add(_documentContext.jobRoot().jobDocument().get(_tabIndex).tabName(), _tabbedPane.get(_tabIndex));
		_tabIndex++;

	}

	@Override
	public void dispose()
	{
		while (_documentContext.isModified()) {
			int answer =
				JOptionPane.showConfirmDialog(this, "Save document before closing?", "Document Not Saved", JOptionPane.YES_NO_CANCEL_OPTION);

			switch (answer) {
				case JOptionPane.CANCEL_OPTION:
					return;

				case JOptionPane.YES_OPTION:
					_documentContext.save();
					break;

				case JOptionPane.NO_OPTION:
					super.dispose();
					return;
			}
		}

		super.dispose();
	}

	private class NewDocumentAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;

		private NewDocumentAction()
		{
			super("New Project");

			putValue(Action.MNEMONIC_KEY, KeyEvent.VK_N);
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_N, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			_documentContext.applicationContext().newDocument();
		}
	}

	private class OpenDocumentAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;

		private OpenDocumentAction()
		{
			super("Open Project");

			putValue(Action.MNEMONIC_KEY, KeyEvent.VK_O);
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			_documentContext.applicationContext().openDocument();
		}
	}

	private class CloseDocumentAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;

		private CloseDocumentAction()
		{
			super("Close Project");

			putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			dispose();
		}
	}

	private class SaveDocumentAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;

		private SaveDocumentAction()
		{
			super("Save Project");

			putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			_documentContext.save();
		}
	}

	private class SaveDocumentAsAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;

		private SaveDocumentAsAction()
		{
			super("Save Project As");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			_documentContext.saveAs();
		}
	}

	private class SaveAllDocumentsAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;

		private SaveAllDocumentsAction()
		{
			super("Save All Projects");

			putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);
			putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.SHIFT_MASK | Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			_documentContext.applicationContext().saveAll();
		}
	}

	public class GenerateJSDLAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;

		private boolean _willLaunch;

		private GenerateJSDLAction(boolean willLaunch)
		{
			super(willLaunch ? "Submit Job" : "Generate JSDL");
			_willLaunch = willLaunch;

			if (_willLaunch) {
				putValue(Action.MNEMONIC_KEY, KeyEvent.VK_J);
				putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_J, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			} else {
				putValue(Action.MNEMONIC_KEY, KeyEvent.VK_G);
				putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_G, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
			}
		};

		public boolean willLaunch()
		{
			return _willLaunch;
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			_documentContext.generateJSDL(_willLaunch);
		}
	}

	static public GridJobToolFrame createNewFrame(JobDocumentContext documentContext)
	{
		OperatingSystemNames os = OperatingSystemNames.mapFromCurrentOperatingSystem();
		if ((os != null) && os.isMacOSX())
			return new MacOSXGridJobToolFrame(documentContext);
		else
			return new WinLinGridJobToolFrame(documentContext);
	}

	private class RecentsListener implements MenuListener
	{
		@Override
		public void menuCanceled(MenuEvent e)
		{
			// Do nothing
		}

		@Override
		public void menuDeselected(MenuEvent e)
		{
			// Do nothing
		}

		@Override
		public void menuSelected(MenuEvent e)
		{
			JMenu recentsMenu = (JMenu) e.getSource();
			recentsMenu.removeAll();
			for (File recent : Recents.instance.recents())
				recentsMenu.add(new RecentAction(recent));
		}
	}

	private class RecentAction extends AbstractAction
	{
		static final long serialVersionUID = 1L;

		private File _file;

		private RecentAction(File file)
		{
			super(file.getAbsolutePath());

			_file = file;
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			_documentContext.applicationContext().openDocument(_file);
		}
	}

	private class PlusAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;

		@Override
		public void actionPerformed(ActionEvent e)
		{
			/*
			 * We get the total number of tabs in the outer tabbed pane (including the Common tab), create a new inner tabbed pane with the
			 * four required tabs (Basic Job Information, Data, Resources and Grid Job Variables) and add a new job description tab to the
			 * outer tabbed pane.
			 */
			if (_tabIndex <= 1) {

				JOptionPane.showMessageDialog(null, "You are moving to a JSDL++ option");
				String tab_name = JOptionPane.showInputDialog("Name of the First Job Description");
				if (tab_name == null || tab_name.equals("")) {
					tab_name = "Job Description " + _tabIndex;
				}
				// Add parameterizable listener for the job variables
				_documentContext.getParameterizableBroker().addParameterizableListener(_documentContext.variableManager());

				// Add Modification Listener for the job variables
				_documentContext.getModificationBroker().addModificationListener(new ModificationListenerImpl());

				_documentContext.jobRoot().jobDocument().add(new JobDocument(tab_name));
				_documentContext.jobRoot().jobDocument().get(_documentContext.jobRoot().jobDocument().size() - 1)
					.postUnmarshall(_documentContext.getParameterizableBroker(), _documentContext.getModificationBroker());

				// Add modification listener for a particular job document's error panel
				ErrorPanel ePanel = new ErrorPanel(_generateJSDL, _documentContext.jobRoot().jobDocument().get(_tabIndex));
				_documentContext.getModificationBroker().addModificationListener(ePanel);
				_ePanels.add(ePanel);

				_superTabbedPane.setTitleAt(0, "common");

				_ePanels.set(0, null);

				// _common.

				// Create the four tabs for the "inner" JTabbedPane and add that to the new "outer"
				// JTabbedPane used for the new job description
				addTabs(_documentContext, _tabIndex);
				_superTabbedPane.add(tab_name, _tabbedPane.get(_tabIndex));

				_tabIndex++;

				// ADD SECOND JOB DESCRIPTION
				String second_tab_name = JOptionPane.showInputDialog("Name of the Second Job Description");
				if (second_tab_name == null || second_tab_name.equals("")) {
					second_tab_name = "Job Description " + _tabIndex;
				}

				_documentContext.getParameterizableBroker().addParameterizableListener(_documentContext.variableManager());

				// Add Modification Listener for the job variables
				_documentContext.getModificationBroker().addModificationListener(new ModificationListenerImpl());

				_documentContext.jobRoot().jobDocument().add(new JobDocument(second_tab_name));
				_documentContext.jobRoot().jobDocument().get(_documentContext.jobRoot().jobDocument().size() - 1)
					.postUnmarshall(_documentContext.getParameterizableBroker(), _documentContext.getModificationBroker());

				// Add modification listener for a particular job document's error panel
				ErrorPanel ePanel1 = new ErrorPanel(_generateJSDL, _documentContext.jobRoot().jobDocument().get(_tabIndex));
				_documentContext.getModificationBroker().addModificationListener(ePanel1);
				_ePanels.add(ePanel1);

				// _superTabbedPane.setTitleAt(0, "common");

				// _common.

				// Create the four tabs for the "inner" JTabbedPane and add that to the new "outer"
				// JTabbedPane used for the new job description
				addTabs(_documentContext, _tabIndex);

				_superTabbedPane.add(second_tab_name, _tabbedPane.get(_tabIndex));

				_tabIndex++;
			} else {

				String tab_name = JOptionPane.showInputDialog("Name of the First Job Description");
				if (tab_name == null || tab_name.equals("")) {
					tab_name = "Job Description " + _tabIndex;
				}
				// Add parameterizable listener for the job variables
				_documentContext.getParameterizableBroker().addParameterizableListener(_documentContext.variableManager());

				// Add Modification Listener for the job variables
				_documentContext.getModificationBroker().addModificationListener(new ModificationListenerImpl());

				_documentContext.jobRoot().jobDocument().add(new JobDocument(tab_name));
				_documentContext.jobRoot().jobDocument().get(_documentContext.jobRoot().jobDocument().size() - 1)
					.postUnmarshall(_documentContext.getParameterizableBroker(), _documentContext.getModificationBroker());

				// Add modification listener for a particular job document's error panel
				ErrorPanel ePanel = new ErrorPanel(_generateJSDL, _documentContext.jobRoot().jobDocument().get(_tabIndex));
				_documentContext.getModificationBroker().addModificationListener(ePanel);
				_ePanels.add(ePanel);

				// Create the four tabs for the "inner" JTabbedPane and add that to the new "outer"
				// JTabbedPane used for the new job description
				addTabs(_documentContext, _tabIndex);
				_superTabbedPane.add(tab_name, _tabbedPane.get(_tabIndex));

				_tabIndex++;
			}

		}

	}

	private class MinusAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;

		private MinusAction()
		{
			// setEnabled(false);
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			int selected = _superTabbedPane.getSelectedIndex();
			System.out.println("Selected index is " + selected);
			System.out.println("Tab index is " + _tabIndex);

			if (selected == 0) {
				JOptionPane.showMessageDialog(_superTabbedPane, "Cannot remove the common tab !!");
				return;
			}

			if (_tabIndex <= 3) {
				int n = JOptionPane.showOptionDialog(null,
					"Going back to legacy JSDL option, Only the contents of the common tab will prevail !!!", "",
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
				if(n == 0){
					if(_documentContext.jobRoot().jobDocument().size() == 3 ){
						_documentContext.jobRoot().jobDocument().remove(2);
						_documentContext.jobRoot().jobDocument().remove(1);
					} else {
						JOptionPane.showMessageDialog(null, "Something is wrong. Can't go back to legacy JSDL");
						return;
					}
					_tabIndex--;
					_tabbedPane.remove(_tabIndex);
					_superTabbedPane.remove(_tabIndex);
	
					_tabIndex--;
					_tabbedPane.remove(_tabIndex);
					_superTabbedPane.remove(_tabIndex);
	
					_superTabbedPane.setTitleAt(0, "Job Description");
	
					ErrorPanel ePanel = new ErrorPanel(_generateJSDL, _documentContext.jobRoot().jobDocument().get(_tabIndex - 1));
					_documentContext.getModificationBroker().addModificationListener(ePanel);
					_ePanels.add(ePanel);
					
				}

				return;
			}

			if(_documentContext.jobRoot().jobDocument().size() > selected ){
				_documentContext.jobRoot().jobDocument().remove(selected);
			} else {
				JOptionPane.showMessageDialog(null, "Something is wrong. Can't remove the selected tab");
				return;
			}
			
			_tabbedPane.remove(selected);
			_superTabbedPane.remove(selected);
			_tabIndex--;
			
			/*
			 * int[] indices = _table.getSelectedRows(); for (int lcv = indices.length - 1; lcv >= 0; lcv--)
			 * _tableModel.removeRow(indices[lcv]);
			 */

		}

	}

	private class ModificationListenerImpl implements ModificationListener
	{

		@Override
		public void jobDescriptionModified()
		{
			_modified = true;
			_initial = false;
			_documentContext.setFrameTitle();
		}
	}

	private class TabbedPaneSelection implements ChangeListener
	{
		int prevIndex = 0;

		public void stateChanged(ChangeEvent e)
		{
			int selectedIndex = _superTabbedPane.getSelectedIndex();
			if (selectedIndex > 0) {
				if (prevIndex > 0)
					remove(_ePanels.get(prevIndex));

				add(_ePanels.get(selectedIndex), new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER,
					GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));

			} else {
				remove(_ePanels.get(prevIndex));
			}
			prevIndex = selectedIndex;

		}
	}
}