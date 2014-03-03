package edu.virginia.vcgr.genii.ui.login;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.JDialog;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import org.morgan.utils.gui.ButtonPanel;

import edu.virginia.vcgr.genii.client.gui.GuiUtils;
import edu.virginia.vcgr.genii.security.credentials.NuCredential;
import edu.virginia.vcgr.genii.ui.UIContext;
import edu.virginia.vcgr.genii.ui.errors.ErrorHandler;

final public class LoginDialog extends JDialog
{
	static final long serialVersionUID = 0L;

	private UIContext _context;
	private Collection<NuCredential> _credentials = null;
	private JTabbedPane _tabbedPane = new JTabbedPane();
	private LoginAction _loginAction = new LoginAction();

	private LoginDialog(Window owner, UIContext uiContext)
	{
		super(owner);

		_context = uiContext;

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		Container content = getContentPane();
		content.setLayout(new GridBagLayout());

		_tabbedPane.add(new IDPLoginPanel());
		_tabbedPane.add(new KeystoreLoginPanel());
		_tabbedPane.add(new UsernamePasswordLoginPanel());

		content.add(_tabbedPane, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
			GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));

		content.add(ButtonPanel.createHorizontalButtonPanel(_loginAction, new CancelAction()), new GridBagConstraints(0, 1, 1,
			1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));
	}

	private class LoginAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;

		private LoginAction()
		{
			super("Login");
		}

		@Override
		final public void actionPerformed(ActionEvent e)
		{
			LoginPanel panel = (LoginPanel) _tabbedPane.getSelectedComponent();

			try {
				_credentials = panel.doLogin(_context);
				if (_credentials != null)
					dispose();
			} catch (Throwable cause) {
				ErrorHandler.handleError(_context, panel, cause);
			}
		}
	}

	private class CancelAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;

		private CancelAction()
		{
			super("Cancel");
		}

		@Override
		final public void actionPerformed(ActionEvent e)
		{
			dispose();
		}
	}

	static public Collection<NuCredential> doLogin(Component responsibleComponent, UIContext context)
	{
		LoginDialog dialog = new LoginDialog(SwingUtilities.getWindowAncestor(responsibleComponent), context);
		dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
		dialog.pack();

		GuiUtils.centerComponent(dialog);
		dialog.setVisible(true);

		return dialog._credentials;
	}
}