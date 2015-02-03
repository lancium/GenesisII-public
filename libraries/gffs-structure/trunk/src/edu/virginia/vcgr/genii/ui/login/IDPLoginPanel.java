package edu.virginia.vcgr.genii.ui.login;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.cmd.tools.BaseGridTool;
import edu.virginia.vcgr.genii.client.cmd.tools.CdTool;
import edu.virginia.vcgr.genii.client.cmd.tools.IDPLoginTool;
import edu.virginia.vcgr.genii.client.cmd.tools.MyProxyLoginTool;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.comm.SecurityUpdateResults;
import edu.virginia.vcgr.genii.client.configuration.DeploymentName;
import edu.virginia.vcgr.genii.client.configuration.Installation;
import edu.virginia.vcgr.genii.client.configuration.NamespaceDefinitions;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
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

	// the kinds of proxies we know about.
	public enum ProxyTypes {
		NO_PROXY,
		XSEDE_MYPROXY,
		LRZ_MYPROXY
	}

	private JTextField _username = new JTextField(16);
	private JTextField _rnsPath = new JTextField(64);
	private JPasswordField _password = new JPasswordField(16);
	private ProxyTypes _type = ProxyTypes.NO_PROXY;

	private String _proxyPort = null;
	private String _proxyHost = null;

	NamespaceDefinitions nsd = Installation.getDeployment(new DeploymentName()).namespace();

	IDPLoginPanel(ProxyTypes proxyType, String Title)
	{
		// If xsede is true we are logging in an XSEDE user and we need to myproxy login first, AND
		// we do not let them set the directory path
		// setName("Standard Grid User");
		setName(Title);
		_type = proxyType;

		_username.addCaretListener(new InternalCaretListener());
		add(new JLabel("Username"), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		add(_username, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
			new Insets(5, 5, 5, 5), 5, 5));
		add(new JLabel("Password"), new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		add(_password, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
			new Insets(5, 5, 5, 5), 5, 5));
		if ((_type != ProxyTypes.XSEDE_MYPROXY) && (_type != ProxyTypes.LRZ_MYPROXY)) {
			add(new JLabel("Grid Path"), new GridBagConstraints(0, 2, 1, 1, 0.0, 1.0, GridBagConstraints.NORTHWEST,
				GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
			add(_rnsPath, new GridBagConstraints(1, 2, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));
		}

		// set up the appropriate myproxy parameters.
		if (_type == ProxyTypes.XSEDE_MYPROXY) {
			_proxyPort = "7512";
			_proxyHost = "myproxy.xsede.org";
		}
		if (_type == ProxyTypes.LRZ_MYPROXY) {
			_proxyPort = "7512";
			_proxyHost = "myproxy.lrz.de";
		}
	}

	@Override
	final public Collection<NuCredential> doLogin(UIContext uiContext) throws Throwable
	{
		Closeable assumedContextToken = null;

		try {
			ICallingContext context = uiContext.callingContext();
			assumedContextToken = ContextManager.temporarilyAssumeContext(context);

			RNSPath path = context.getCurrentPath().lookup(_rnsPath.getText(), RNSPathQueryFlags.DONT_CARE);
			if (!path.exists()) {
				JOptionPane.showMessageDialog(this, "No such user: grid path doesn't exist!", "Unknown User Path",
					JOptionPane.ERROR_MESSAGE);
				return null;
			}

			UsernamePasswordIdentity upt = null;
			TransientCredentials transientCredentials = null;

			try {
				// we're going to use the WS-TRUST token-issue operation
				// to log in to a security tokens service
				KeyAndCertMaterial clientKeyMaterial =
					ClientUtils.checkAndRenewCredentials(context, BaseGridTool.credsValidUntil(), new SecurityUpdateResults());

				if ((_type == ProxyTypes.XSEDE_MYPROXY) || (_type == ProxyTypes.LRZ_MYPROXY)) {
					MyProxyLoginTool lt = new MyProxyLoginTool();
					lt.establishStandardIO(null, null, null);
					lt.setUsername(_username.getText());
					lt.setPassword(new String(_password.getPassword()));
					lt.setPort(_proxyPort);
					lt.setHost(_proxyHost);

					lt.doMyproxyLogin(context);

					// System.out.println("before reloading have cert: " +
					// clientKeyMaterial._clientCertChain[0].getSubjectDN());

					// reload the key material after myproxy changes it.
					clientKeyMaterial =
						ClientUtils.checkAndRenewCredentials(context, BaseGridTool.credsValidUntil(),
							new SecurityUpdateResults());
				}

				upt = new UsernamePasswordIdentity(_username.getText(), new String(_password.getPassword()));

				transientCredentials = TransientCredentials.getTransientCredentials(context);
				transientCredentials.add(upt);

				ContextManager.storeCurrentContext(context);

				EndpointReferenceType epr = path.getEndpoint();

				// System.out.println("about to do idplogin with cert: " +
				// clientKeyMaterial._clientCertChain[0].getSubjectDN());

				// Do IDP login
				ArrayList<NuCredential> creds =
					IDPLoginTool.doIdpLogin(epr, SecurityConstants.CredentialExpirationMillis,
						clientKeyMaterial._clientCertChain);

				/*
				 * try to leave the user in the right current directory. Changed by ASAG March 6,
				 * 2014
				 */
				{
					// Assumption is that user idp's are off /user and homes off /home
					String userHome = path.getName().replaceFirst("user", "home");
					try {
						CdTool.chdir(userHome);
					} catch (Throwable e) {
					}
				}

				return creds;
			} finally {
				if ((upt != null) && (transientCredentials != null)) {
					// the UT credential was used only to log into the IDP, remove it
					transientCredentials.remove(upt);
				}
			}
		} finally {
			StreamUtils.close(assumedContextToken);
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
			_rnsPath.setText(nsd.getUsersDirectory() + "/" + _username.getText());
		}
	}
}
