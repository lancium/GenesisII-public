package edu.virginia.vcgr.genii.gjt.gui;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import edu.virginia.vcgr.genii.client.gui.GuiHelpAction;
import edu.virginia.vcgr.genii.client.gui.HelpLinkConfiguration;
import edu.virginia.vcgr.genii.gjt.JobDocumentContext;

class WinLinGridJobToolFrame extends GridJobToolFrame
{
	static final long serialVersionUID = 0L;

	@Override
	protected Action getPreferencesAction()
	{
		return new PreferencesAction();
	}

	@Override
	protected Action getAboutAction()
	{
		return new AboutAction();
	}

	@Override
	protected Action getExitAction()
	{
		return new ExitAction();
	}

	WinLinGridJobToolFrame(JobDocumentContext documentContext)
	{
		super(documentContext);
	}

	private class PreferencesAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;

		private PreferencesAction()
		{
			super("Preferences");

			putValue(Action.MNEMONIC_KEY, KeyEvent.VK_P);
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_COMMA, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			_documentContext.applicationContext().showPreferences();
		}
	}

	private class ExitAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;

		private ExitAction()
		{
			super("Exit");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			_documentContext.applicationContext().exit();
		}
	}

	private class AboutAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;

		private AboutAction()
		{
			super("Help");
		}

		@Override
		public void actionPerformed(ActionEvent e)
		{
			// Changed 2/10/2016 by ASG to go to the web page and not a local hard-wired text.
			GuiHelpAction.DisplayUrlHelp(HelpLinkConfiguration.get_help_url(HelpLinkConfiguration.JOB_CREATE_HELP));
			// _documentContext.applicationContext().showAbout();
		}
	}
}
