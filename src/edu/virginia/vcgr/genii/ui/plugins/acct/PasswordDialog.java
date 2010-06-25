package edu.virginia.vcgr.genii.ui.plugins.acct;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.SwingUtilities;

import org.morgan.utils.gui.ButtonPanel;

import edu.virginia.vcgr.genii.client.gui.GuiUtils;

class PasswordDialog extends JDialog
{
	static final long serialVersionUID = 0L;
	
	private JPasswordField _passwordField = new JPasswordField(16);
	private String _password = null;
	
	private PasswordDialog(Component ownerComponent)
	{
		super(SwingUtilities.getWindowAncestor(ownerComponent),
			"Accounting DB Password");
		
		Container content = getContentPane();
		content.setLayout(new GridBagLayout());
		
		content.add(
			new JLabel("Please enter password for accounting database."), 
			new GridBagConstraints(0, 0, 1, 1, 1.0, 0.0, 
				GridBagConstraints.WEST, GridBagConstraints.NONE, 
				new Insets(5, 5, 5, 5), 5, 5));
		content.add(_passwordField, 
			new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0,
				GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));
		
		JButton okButton = new JButton(new OKAction());
		content.add(ButtonPanel.createHorizontalButtonPanel(okButton,
			new CancelAction()), new GridBagConstraints(
				0, 2, 1, 1, 1.0, 1.0, GridBagConstraints.SOUTH,
				GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));
		
		getRootPane().setDefaultButton(okButton);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}
	
	private class OKAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;
		
		private OKAction()
		{
			super("OK");
		}
		
		@Override
		public void actionPerformed(ActionEvent e)
		{
			_password = new String(_passwordField.getPassword());
			dispose();
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
		public void actionPerformed(ActionEvent e)
		{
			_password = null;
			dispose();
		}
	}
	
	static String getPassword(Component ownerComponent)
	{
		PasswordDialog dialog = new PasswordDialog(ownerComponent);
		dialog.pack();
		dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
		GuiUtils.centerComponent(dialog);
		dialog.setVisible(true);
		return dialog._password;
	}
}