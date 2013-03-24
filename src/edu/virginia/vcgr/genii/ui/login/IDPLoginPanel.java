package edu.virginia.vcgr.genii.ui.login;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.cmd.tools.IDPLoginTool;
import edu.virginia.vcgr.genii.client.cmd.tools.LoginTool;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.comm.SecurityUpdateResults;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.security.SecurityConstants;
import edu.virginia.vcgr.genii.security.TransientCredentials;
import edu.virginia.vcgr.genii.security.credentials.NuCredential;
import edu.virginia.vcgr.genii.security.credentials.identity.UsernamePasswordIdentity;
import edu.virginia.vcgr.genii.security.x509.KeyAndCertMaterial;
import edu.virginia.vcgr.genii.ui.UIContext;

final class IDPLoginPanel extends LoginPanel
{
	static final long serialVersionUID = 0L;

	private JTextField _username = new JTextField(16);
	private JTextField _rnsPath = new JTextField(25);
	private JPasswordField _password = new JPasswordField(16);

	IDPLoginPanel()
	{
		setName("Standard Grid User");

		_username.addCaretListener(new InternalCaretListener());
		add(new JLabel("Username"), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		add(_username, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
			new Insets(5, 5, 5, 5), 5, 5));
		add(new JLabel("Password"), new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		add(_password, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
			new Insets(5, 5, 5, 5), 5, 5));
		add(new JLabel("Grid Path"), new GridBagConstraints(0, 2, 1, 1, 0.0, 1.0, GridBagConstraints.NORTHWEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		add(_rnsPath, new GridBagConstraints(1, 2, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
			new Insets(5, 5, 5, 5), 5, 5));
	}

	@Override
	final public Collection<NuCredential> doLogin(UIContext uiContext) throws Throwable
	{
		Closeable token = null;

		try {
			ContextManager.temporarilyAssumeContext(uiContext.callingContext());
			RNSPath path = uiContext.callingContext().getCurrentPath().lookup(_rnsPath.getText(), RNSPathQueryFlags.DONT_CARE);
			if (!path.exists()) {
				JOptionPane.showMessageDialog(this, "Grid path doesn't exist!", "Bad Grid Path", JOptionPane.ERROR_MESSAGE);
				return null;
			}

			UsernamePasswordIdentity upt = new UsernamePasswordIdentity(_username.getText(),
				new String(_password.getPassword()));

			TransientCredentials transientCredentials = TransientCredentials
				.getTransientCredentials(uiContext.callingContext());
			transientCredentials.add(upt);

			try {
				// we're going to use the WS-TRUST token-issue operation
				// to log in to a security tokens service
				KeyAndCertMaterial clientKeyMaterial = ClientUtils.checkAndRenewCredentials(uiContext.callingContext(),
					new Date(), new SecurityUpdateResults());

				EndpointReferenceType epr = path.getEndpoint();

				// Do IDP login
				ArrayList<NuCredential> creds = IDPLoginTool.doIdpLogin(epr, SecurityConstants.CredentialExpirationMillis,
					clientKeyMaterial._clientCertChain);

				// try to leave the user in the right current directory.
				LoginTool.jumpToUserHomeIfExists(_username.getText());

				return creds;
			} finally {
				if (upt != null) {
					// the UT credential was used only to log into the IDP, remove it
					transientCredentials.remove(upt);
				}
			}
		} finally {
			StreamUtils.close(token);
		}
	}

	@Override
	final public boolean isLoginInformationValid()
	{
		return _rnsPath.getText().length() > 0;
	}

	private class InternalCaretListener implements CaretListener
	{
		@Override
		final public void caretUpdate(CaretEvent e)
		{
			_rnsPath.setText("/users/" + _username.getText());
		}
	}
}