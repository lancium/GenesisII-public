package org.morgan.mac.gui.swing;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;

import org.morgan.mac.gui.macwrap.Application;
import org.morgan.mac.gui.macwrap.ApplicationAdapter;
import org.morgan.mac.gui.macwrap.ApplicationEvent;
import org.morgan.mac.gui.macwrap.MacOSXGuiSetup;

public class TestDialog {
	static public void main(String[] args) {
		MacOSXGuiSetup.setupMacOSXGuiApplication("Test Dialog", true, false,
				true, true);

		JFrame dialog = new JFrame();
		dialog.setTitle("This is a test.");
		Container content = dialog.getContentPane();
		content.setLayout(new GridBagLayout());

		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("File");
		menuBar.add(menu);
		dialog.setJMenuBar(menuBar);

		content.add(new JLabel("Hello, World!"), new GridBagConstraints(0, 0,
				1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
				GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		dialog.pack();

		Application application = Application.getApplication();
		application.addApplicationListener(new ApplicationAdapterListener(
				dialog));
		dialog.setVisible(true);

		application.setDockIconBadge("Mark");
	}

	static private class ApplicationAdapterListener extends ApplicationAdapter {
		private JFrame _dialog;

		private ApplicationAdapterListener(JFrame dialog) {
			_dialog = dialog;
		}

		@Override
		public void handleQuit(ApplicationEvent event) {
			System.err.println("Asked to quit from the OS.");
			_dialog.dispose();
		}
	}
}