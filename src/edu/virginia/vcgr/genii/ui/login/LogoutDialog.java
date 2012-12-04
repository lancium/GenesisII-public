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
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.morgan.utils.gui.ButtonPanel;

import edu.virginia.vcgr.genii.client.gui.GuiUtils;
import edu.virginia.vcgr.genii.security.VerbosityLevel;
import edu.virginia.vcgr.genii.security.credentials.GIICredential;

@SuppressWarnings("rawtypes")
class LogoutDialog extends JDialog
{
	static final long serialVersionUID = 0L;
	
	static private class CredentialWrapper
	{
		private GIICredential _credential;
		
		private CredentialWrapper(GIICredential credential)
		{
			_credential = credential;
		}
		
		@Override
		final public String toString()
		{
			return _credential.describe(VerbosityLevel.LOW);
		}
	}
	
	static private Object[] createCredentialList(
		Collection<GIICredential> credList)
	{
		Object []ret = new Object[credList.size()];
		int lcv = 0;
		for (GIICredential cred : credList)
			ret[lcv++] = new CredentialWrapper(cred);
		
		return ret;
	}
	
	private class LogoutAction extends AbstractAction
		implements ListSelectionListener
	{
		static final long serialVersionUID = 0L;
		
		private void evaluateStatus()
		{
			setEnabled(_credentialList.getSelectedIndices().length > 0);
		}
		
		private LogoutAction()
		{
			super("Logout Selected Credentials");
			
			evaluateStatus();
		}
		
		@SuppressWarnings("deprecation")
        @Override
		final public void actionPerformed(ActionEvent e)
		{
			int answer = JOptionPane.showConfirmDialog(
				(Component)e.getSource(),
				"Log out from selected credentials?",
				"Logout Confirmation",
				JOptionPane.YES_NO_OPTION);
			if (answer == JOptionPane.YES_OPTION)
			{
				Object []values = _credentialList.getSelectedValues();
				GIICredential []logoutItems = new GIICredential[values.length];
				
				for (int lcv = 0; lcv < values.length; lcv++)
					logoutItems[lcv] =
						((CredentialWrapper)values[lcv])._credential;
				
				_context.logout(LogoutDialog.this, logoutItems);
				
				dispose();
			}
		}

		@Override
		final public void valueChanged(ListSelectionEvent e)
		{
			evaluateStatus();
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
	
	private CredentialManagementContext _context;
	
	private JList _credentialList;
	
	@SuppressWarnings("unchecked")
    LogoutDialog(Window owner, CredentialManagementContext context)
	{
		super(owner, "Logout Dialog", ModalityType.DOCUMENT_MODAL);
		_context = context;
		
		_credentialList = new JList(createCredentialList(
			context.loginItems()));
		
		LogoutAction logout = new LogoutAction();
		CancelAction cancel = new CancelAction();
		
		_credentialList.addListSelectionListener(logout);
		
		Container content = getContentPane();
		content.setLayout(new GridBagLayout());
		content.add(new JScrollPane(_credentialList), new GridBagConstraints(
			0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
			GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 5, 5));
		content.add(ButtonPanel.createHorizontalButtonPanel(logout, cancel),
			new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack();
		GuiUtils.centerComponent(this);
		setVisible(true);
	}
	
	LogoutDialog(Component owner, CredentialManagementContext context)
	{
		this(SwingUtilities.getWindowAncestor(owner), context);
	}
}