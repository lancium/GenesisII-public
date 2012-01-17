package edu.virginia.vcgr.genii.ui.login;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.cmd.tools.gamllogin.CertEntry;
import edu.virginia.vcgr.genii.client.cmd.tools.gamllogin.GuiGamlLoginHandler;
import edu.virginia.vcgr.genii.security.credentials.GIICredential;
import edu.virginia.vcgr.genii.security.credentials.assertions.BasicConstraints;
import edu.virginia.vcgr.genii.security.credentials.assertions.RenewableClientAssertion;
import edu.virginia.vcgr.genii.security.credentials.assertions.RenewableClientAttribute;
import edu.virginia.vcgr.genii.security.credentials.identity.X509Identity;
import edu.virginia.vcgr.genii.ui.UIContext;

final class KeystoreLoginPanel extends LoginPanel
{
	static final long serialVersionUID = 0L;
	
	static final private String NAME = "Local Keystore";
	
	private JFileChooser _fileChooser = new JFileChooser();
	private JTextField _keystoreFile = new JTextField(16);
	private JPasswordField _password = new JPasswordField(16);
	
	KeystoreLoginPanel()
	{
		setName(NAME);
		
		_fileChooser.setAcceptAllFileFilterUsed(true);
		_fileChooser.setDialogTitle("Select Keystore File");
		_fileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
		_fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		add(new JLabel("Keystore Path"), new GridBagConstraints(
			0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		add(_keystoreFile, new GridBagConstraints(
			1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.NORTH,
			GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));
		add(new JButton(new BrowseAction()), new GridBagConstraints(
			2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		add(new JLabel("Keystore Password"), new GridBagConstraints(
			0, 1, 1, 1, 0.0, 1.0, GridBagConstraints.NORTHWEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		add(_password, new GridBagConstraints(
			1, 1, 2, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST,
			GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 5, 5));
	}
	
	@Override
	public Collection<GIICredential> doLogin(UIContext uiContext) 
		throws Throwable
	{
		if (_keystoreFile.getText().length() == 0)
			JOptionPane.showMessageDialog(this, "Keystore Path cannot be empty!", 
				"Invalid Login", JOptionPane.ERROR_MESSAGE);
		else
		{
			InputStream inputStreamFile = null;
			try
			{
				inputStreamFile = new FileInputStream(_keystoreFile.getText());
				GuiGamlLoginHandler handler = new GuiGamlLoginHandler(null, null, null);
				CertEntry entry = handler.selectCert(inputStreamFile, "PKCS12",
					new String(_password.getPassword()), false, null);
				if (entry != null)
				{
					// Create identity assertion
					X509Identity identityAssertion = 
						new X509Identity(entry._certChain);
					RenewableClientAttribute delegateeAttribute = new RenewableClientAttribute(
						new BasicConstraints(
							System.currentTimeMillis() - GenesisIIConstants.CredentialGoodFromOffset,
							GenesisIIConstants.CredentialExpirationMillis, 										
							10),												 
							identityAssertion,
							uiContext.callingContext());

					// Delegate the identity assertion to the temporary client
					// identity
					Collection<GIICredential> ret = 
						new ArrayList<GIICredential>(1);
					ret.add(new RenewableClientAssertion(delegateeAttribute, 
						entry._privateKey));
					return ret;
				}
			}
			catch (FileNotFoundException fnfe)
			{
				JOptionPane.showMessageDialog(this,
					"Couldn't locate keystore file!", "Unable to Login",
					JOptionPane.ERROR_MESSAGE);
			}
			finally
			{
				StreamUtils.close(inputStreamFile);
			}
		}
		
		return null;
	}

	@Override
	public boolean isLoginInformationValid()
	{
		return _keystoreFile.getText().length() > 0;
	}
	
	final private class BrowseAction extends AbstractAction
	{
		static final long serialVersionUID = 0L;
		
		private BrowseAction()
		{
			super("Browse");
		}
		
		@Override
		final public void actionPerformed(ActionEvent e)
		{
			int result = _fileChooser.showOpenDialog(KeystoreLoginPanel.this);
			if (result == JFileChooser.APPROVE_OPTION)
				_keystoreFile.setText(_fileChooser.getSelectedFile().getAbsolutePath());
		}
	}
}