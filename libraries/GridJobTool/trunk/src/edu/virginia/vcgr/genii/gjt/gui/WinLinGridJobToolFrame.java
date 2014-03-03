package edu.virginia.vcgr.genii.gjt.gui;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import edu.virginia.vcgr.genii.gjt.JobDocumentContext;

class WinLinGridJobToolFrame extends GridJobToolFrame {
	static final long serialVersionUID = 0L;

	@Override
	protected Action getPreferencesAction() {
		return new PreferencesAction();
	}

	@Override
	protected Action getAboutAction() {
		return new AboutAction();
	}

	@Override
	protected Action getExitAction() {
		return new ExitAction();
	}

	WinLinGridJobToolFrame(JobDocumentContext documentContext) {
		super(documentContext);
	}

	private class PreferencesAction extends AbstractAction {
		static final long serialVersionUID = 0L;

		private PreferencesAction() {
			super("Preferences");

			putValue(Action.MNEMONIC_KEY, KeyEvent.VK_P);
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
					KeyEvent.VK_COMMA, Toolkit.getDefaultToolkit()
							.getMenuShortcutKeyMask()));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			_documentContext.applicationContext().showPreferences();
		}
	}

	private class ExitAction extends AbstractAction {
		static final long serialVersionUID = 0L;

		private ExitAction() {
			super("Exit");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			_documentContext.applicationContext().exit();
		}
	}

	private class AboutAction extends AbstractAction {
		static final long serialVersionUID = 0L;

		private AboutAction() {
			super("About");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			_documentContext.applicationContext().showAbout();
		}
	}
}
