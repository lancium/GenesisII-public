package edu.virginia.vcgr.genii.gjt.gui;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;

import edu.virginia.vcgr.genii.gjt.JobDocumentContext;
import edu.virginia.vcgr.genii.gjt.data.recent.Recents;
import edu.virginia.vcgr.genii.gjt.gui.basic.BasicJobInformation;
import edu.virginia.vcgr.genii.gjt.gui.fs.FilesystemMenu;
import edu.virginia.vcgr.genii.gjt.gui.resource.ResourcesPanel;
import edu.virginia.vcgr.genii.gjt.gui.stage.DataTab;
import edu.virginia.vcgr.genii.gjt.gui.variables.VariablePanel;
import edu.virginia.vcgr.jsdl.OperatingSystemNames;

public abstract class GridJobToolFrame extends JFrame {
	static final long serialVersionUID = 0L;

	protected JobDocumentContext _documentContext;

	private JTabbedPane _tabbedPane = new JTabbedPane();

	private void addMenu(JMenuBar bar, JMenu menu) {
		if (bar != null && menu != null)
			bar.add(menu);
	}

	protected GridJobToolFrame(JobDocumentContext documentContext) {
		GenerateJSDLAction generateJSDL = new GenerateJSDLAction(
				documentContext.applicationContext().willLaunch());

		ErrorPanel ePanel = new ErrorPanel(generateJSDL,
				documentContext.jobDocument());
		documentContext.getModificationBroker().addModificationListener(ePanel);

		_documentContext = documentContext;

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		Container container = getContentPane();
		container.setLayout(new GridBagLayout());

		add(_tabbedPane, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						5, 5, 5, 5), 5, 5));
		add(ePanel, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));

		addTabs(documentContext);

		JMenuBar menuBar = createMenuBar(generateJSDL);
		if (menuBar != null)
			setJMenuBar(menuBar);
	}

	abstract protected Action getExitAction();

	abstract protected Action getPreferencesAction();

	abstract protected Action getAboutAction();

	protected JMenu createRecentsMenu() {
		JMenu ret = new JMenu("Recents");
		ret.addMenuListener(new RecentsListener());

		return ret;
	}

	protected JMenu createFileMenu(AbstractAction generateJSDLAction) {
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

	protected JMenuBar createMenuBar(GenerateJSDLAction generateJSDLAction) {
		JMenuBar bar = new JMenuBar();
		addMenu(bar, createFileMenu(generateJSDLAction));
		addMenu(bar, new FilesystemMenu(_documentContext.jobDocument()
				.filesystemMap()));

		Action aboutAction = getAboutAction();
		if (aboutAction != null) {
			JMenu helpMenu = new JMenu("Help");
			helpMenu.add(aboutAction);
			bar.add(Box.createHorizontalGlue());
			addMenu(bar, helpMenu);
		}

		return bar;
	}

	protected void addTabs(JobDocumentContext documentContext) {
		_tabbedPane.add(new BasicJobInformation(documentContext));
		_tabbedPane.add(new DataTab(documentContext));
		_tabbedPane.add(new ResourcesPanel(documentContext));
		VariablePanel vp = new VariablePanel(_tabbedPane, documentContext);
		_tabbedPane.add(vp);
		_tabbedPane.setEnabledAt(_tabbedPane.indexOfComponent(vp),
				vp.isEnabled());
	}

	@Override
	public void dispose() {
		while (_documentContext.isModified()) {
			int answer = JOptionPane.showConfirmDialog(this,
					"Save document before closing?", "Document Not Saved",
					JOptionPane.YES_NO_CANCEL_OPTION);

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

	private class NewDocumentAction extends AbstractAction {
		static final long serialVersionUID = 0L;

		private NewDocumentAction() {
			super("New Project");

			putValue(Action.MNEMONIC_KEY, KeyEvent.VK_N);
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_N, Toolkit.getDefaultToolkit()
							.getMenuShortcutKeyMask()));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			_documentContext.applicationContext().newDocument();
		}
	}

	private class OpenDocumentAction extends AbstractAction {
		static final long serialVersionUID = 0L;

		private OpenDocumentAction() {
			super("Open Project");

			putValue(Action.MNEMONIC_KEY, KeyEvent.VK_O);
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_O, Toolkit.getDefaultToolkit()
							.getMenuShortcutKeyMask()));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			_documentContext.applicationContext().openDocument();
		}
	}

	private class CloseDocumentAction extends AbstractAction {
		static final long serialVersionUID = 0L;

		private CloseDocumentAction() {
			super("Close Project");

			putValue(Action.MNEMONIC_KEY, KeyEvent.VK_C);
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_W, Toolkit.getDefaultToolkit()
							.getMenuShortcutKeyMask()));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			dispose();
		}
	}

	private class SaveDocumentAction extends AbstractAction {
		static final long serialVersionUID = 0L;

		private SaveDocumentAction() {
			super("Save Project");

			putValue(Action.MNEMONIC_KEY, KeyEvent.VK_S);
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_S, Toolkit.getDefaultToolkit()
							.getMenuShortcutKeyMask()));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			_documentContext.save();
		}
	}

	private class SaveDocumentAsAction extends AbstractAction {
		static final long serialVersionUID = 0L;

		private SaveDocumentAsAction() {
			super("Save Project As");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			_documentContext.saveAs();
		}
	}

	private class SaveAllDocumentsAction extends AbstractAction {
		static final long serialVersionUID = 0L;

		private SaveAllDocumentsAction() {
			super("Save All Projects");

			putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);
			putValue(
					Action.ACCELERATOR_KEY,
					KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.SHIFT_MASK
							| Toolkit.getDefaultToolkit()
									.getMenuShortcutKeyMask()));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			_documentContext.applicationContext().saveAll();
		}
	}

	private class GenerateJSDLAction extends AbstractAction {
		static final long serialVersionUID = 0L;

		private GenerateJSDLAction(boolean willLaunch) {
			super(willLaunch ? "Submit Job" : "Generate JSDL");

			putValue(Action.MNEMONIC_KEY, KeyEvent.VK_G);
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_G, Toolkit.getDefaultToolkit()
							.getMenuShortcutKeyMask()));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			_documentContext.generateJSDL();
		}
	}

	static public GridJobToolFrame createNewFrame(
			JobDocumentContext documentContext) {
		OperatingSystemNames os = OperatingSystemNames
				.getCurrentOperatingSystem();

		if (os == OperatingSystemNames.MACOS)
			return new MacOSXGridJobToolFrame(documentContext);
		else
			return new WinLinGridJobToolFrame(documentContext);
	}

	private class RecentsListener implements MenuListener {
		@Override
		public void menuCanceled(MenuEvent e) {
			// Do nothing
		}

		@Override
		public void menuDeselected(MenuEvent e) {
			// Do nothing
		}

		@Override
		public void menuSelected(MenuEvent e) {
			JMenu recentsMenu = (JMenu) e.getSource();
			recentsMenu.removeAll();
			for (File recent : Recents.instance.recents())
				recentsMenu.add(new RecentAction(recent));
		}
	}

	private class RecentAction extends AbstractAction {
		static final long serialVersionUID = 1L;

		private File _file;

		private RecentAction(File file) {
			super(file.getAbsolutePath());

			_file = file;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			_documentContext.applicationContext().openDocument(_file);
		}
	}
}