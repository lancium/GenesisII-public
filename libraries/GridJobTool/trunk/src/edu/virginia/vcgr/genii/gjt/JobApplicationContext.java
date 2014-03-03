package edu.virginia.vcgr.genii.gjt;

import java.awt.Component;
import java.awt.Dialog.ModalityType;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;
import org.morgan.mac.gui.macwrap.Application;
import org.morgan.mac.gui.macwrap.ApplicationAdapter;
import org.morgan.mac.gui.macwrap.ApplicationEvent;
import org.morgan.mac.gui.macwrap.MacOSXGuiSetup;

import edu.virginia.vcgr.genii.gjt.gui.about.AboutDialog;
import edu.virginia.vcgr.genii.gjt.gui.prefs.PreferencesEditor;
import edu.virginia.vcgr.genii.gjt.gui.util.GUIUtils;
import edu.virginia.vcgr.genii.gjt.prefs.ToolPreferences;
import edu.virginia.vcgr.jsdl.OperatingSystemNames;

public class JobApplicationContext {
	static private Logger _logger = Logger
			.getLogger(JobApplicationContext.class);

	static private ProjectFileFilter pff = new ProjectFileFilter();
	static private JSDLFileFilter jff = new JSDLFileFilter();

	private ToolPreferences _preferences;
	private JFileChooser _fileChooser;
	private JobDefinitionListener _generationListener;
	private Collection<JobDocumentContext> _openDocuments = new LinkedList<JobDocumentContext>();

	JobApplicationContext(Collection<File> initialFiles,
			JobDefinitionListener generationListener,
			JobToolListener toolListener) throws IOException {
		_preferences = new ToolPreferences();

		if (OperatingSystemNames.getCurrentOperatingSystem() == OperatingSystemNames.MACOS) {
			MacOSXGuiSetup.setupMacOSXGuiApplication("Grid Job Tool", true,
					true, true, false);

			Application.getApplication().setEnabledPreferencesMenu(true);
			Application.getApplication().setEnabledAboutMenu(true);
			Application.getApplication().addApplicationListener(
					new MacOSXApplicationListener());
		}

		_fileChooser = new JFileChooser();
		_fileChooser.setAcceptAllFileFilterUsed(true);
		_fileChooser.setMultiSelectionEnabled(false);
		_fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		_generationListener = generationListener;

		if (initialFiles != null && initialFiles.size() > 0) {
			for (File initialFile : initialFiles)
				_openDocuments.add(new JobDocumentContext(this, initialFile,
						toolListener));
		} else {
			JobDocumentContext ctxt = new JobDocumentContext(this, null,
					toolListener);
			ctxt.setInitial();
			_openDocuments.add(ctxt);
		}
	}

	void start() {
		for (JobDocumentContext context : _openDocuments)
			context.start();
	}

	File getDesiredFile(Component parentComponent, boolean isOpen,
			boolean isGenerateTarget) {
		_fileChooser.removeChoosableFileFilter(pff);
		_fileChooser.removeChoosableFileFilter(jff);
		_fileChooser.addChoosableFileFilter(isGenerateTarget ? jff : pff);
		_fileChooser.setFileFilter(isGenerateTarget ? jff : pff);

		int result;

		if (isOpen)
			result = _fileChooser.showOpenDialog(parentComponent);
		else
			result = _fileChooser.showSaveDialog(parentComponent);

		if (result == JFileChooser.APPROVE_OPTION)
			return _fileChooser.getSelectedFile();

		return null;
	}

	JobDefinitionListener getGenerationListener() {
		return _generationListener;
	}

	public boolean willLaunch() {
		return _generationListener != null;
	}

	public ToolPreferences preferences() {
		return _preferences;
	}

	public void newDocument() {
		try {
			JobDocumentContext ctxt;
			_openDocuments.add(ctxt = new JobDocumentContext(this, null, null));
			ctxt.start();
		} catch (IOException ioe) {
			// Won't happen since we aren't reading anything.
			_logger.fatal("This shouldn't have happened.", ioe);
		}
	}

	public void openDocument(File source) {
		try {
			JobDocumentContext ctxt;
			ctxt = new JobDocumentContext(this, source, null);
			if (_openDocuments.size() == 1
					&& _openDocuments.iterator().next().isInitial()) {
				_openDocuments.iterator().next().close();
				_openDocuments.clear();
			}
			_openDocuments.add(ctxt);
			ctxt.start();
		} catch (IOException ioe) {
			JOptionPane.showMessageDialog(null, "Error opening project file.",
					"Error Opening Project File", JOptionPane.ERROR_MESSAGE);
			_logger.error("Unable to open project file.", ioe);
		}
	}

	public void openDocument() {
		File source = getDesiredFile(null, true, false);
		if (source != null)
			openDocument(source);
	}

	public void saveAll() {
		for (JobDocumentContext context : _openDocuments)
			context.save();
	}

	public void exit() {
		for (JobDocumentContext jdc : _openDocuments)
			jdc.close();
	}

	public void showPreferences() {
		PreferencesEditor editor = new PreferencesEditor(_preferences);
		editor.setModalityType(ModalityType.APPLICATION_MODAL);
		editor.pack();
		GUIUtils.centerComponent(editor);
		editor.setVisible(true);
	}

	public void showAbout() {
		AboutDialog dialog = new AboutDialog(null);
		dialog.pack();
		dialog.setModalityType(ModalityType.APPLICATION_MODAL);
		GUIUtils.centerComponent(dialog);
		dialog.setVisible(true);
	}

	private class MacOSXApplicationListener extends ApplicationAdapter {
		@Override
		public void handleOpenFile(ApplicationEvent event) {
			try {
				JobDocumentContext ctxt;
				_openDocuments.add(ctxt = new JobDocumentContext(
						JobApplicationContext.this, new File(event
								.getFilename()), null));
				ctxt.start();
			} catch (IOException ioe) {
				JOptionPane
						.showMessageDialog(null, "Error opening project file.",
								"Error Opening Project File",
								JOptionPane.ERROR_MESSAGE);
				_logger.error("Unable to open project file.", ioe);
			}
		}

		@Override
		public void handlePreferences(ApplicationEvent event) {
			showPreferences();
		}

		@Override
		public void handleAbout(ApplicationEvent event) {
			showAbout();
			event.setHandled(true);
		}

		@Override
		public void handleQuit(ApplicationEvent event) {
			exit();
		}
	}

	static private class ProjectFileFilter extends FileFilter {
		@Override
		public boolean accept(File f) {
			boolean ret = f.getName().endsWith(".gjp");
			return ret;

		}

		@Override
		public String getDescription() {
			return "Grid Job Project (.gjp)";
		}
	}

	static private class JSDLFileFilter extends FileFilter {
		@Override
		public boolean accept(File f) {
			return f.getName().endsWith(".jsdl");
		}

		@Override
		public String getDescription() {
			return "JSDL File (.jsdl)";
		}
	}
}