package edu.virginia.vcgr.genii.ui.login;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Closeable;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.InstallationProperties;
import edu.virginia.vcgr.genii.client.cmd.tools.BaseGridTool;
import edu.virginia.vcgr.genii.client.cmd.tools.CdTool;
import edu.virginia.vcgr.genii.client.cmd.tools.IDPLoginTool;
import edu.virginia.vcgr.genii.client.cmd.tools.MyProxyLoginTool;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.comm.SecurityUpdateResults;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.configuration.DeploymentName;
import edu.virginia.vcgr.genii.client.configuration.Installation;
import edu.virginia.vcgr.genii.client.configuration.NamespaceDefinitions;
import edu.virginia.vcgr.genii.client.configuration.UserConfigurationFile;
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
	static private Log _logger = LogFactory.getLog(IDPLoginPanel.class);

	//future: move these to a more general login related constants file.
	final public String DEFAULT_LRZ_USER_PATH = "/users/gffs.eu/lrz.de";
	final public String DEFAULT_MYPROXY_USER_PATH = "/users/xsede.org";

	// future: move this to a more useful place, so other classes can rely on it.
	final public static String USER_SECURITY_PROPERTIES_FILE = "clientui-settings.properties";

	// the last setting that the user had chosen for the login proxy.
	final public static String LOGIN_PROXY_SETTING = "ui.login-proxy";

	private String _currentStsPath = "";
	private JTextField _username = new JTextField(16);
	private JTextField _rnsPath = new JTextField(64);
	private JPasswordField _password = new JPasswordField(16);
	private ProxyTypes _type = ProxyTypes.NO_PROXY;
	// combo box values: 0 is none, 1 is xsede, 2 is lrz.
	private JComboBox<String> _comboBox = new JComboBox<String>(ProxyTypes.getAllValues());
	private UserConfigurationFile _loginSettings = null;

	private String _proxyPort = null;
	private String _proxyHost = null;

	NamespaceDefinitions _nsd = Installation.getDeployment(new DeploymentName()).namespace();

	// the kinds of proxies we know about.
	public enum ProxyTypes {
		NO_PROXY("No Proxy"),
		XSEDE_MYPROXY("XSEDE MyProxy"),
		LRZ_MYPROXY("LRZ MyProxy"),
		NO_PROXY_ALT("NO_PROXY"),
		XSEDE_MYPROXY_ALT("XSEDE_MYPROXY"),
		LRZ_MYPROXY_ALT("LRZ_MYPROXY");

		String _value = null;

		ProxyTypes(String value)
		{
			_value = value;
		}

		public String getValue()
		{
			return _value;
		}

		@Override
		public String toString()
		{
			return _value;
		}

		public static ProxyTypes parseString(String typeName)
		{
			if (NO_PROXY._value.equalsIgnoreCase(typeName) || NO_PROXY_ALT._value.equalsIgnoreCase(typeName)) {
				return NO_PROXY;
			} else if (XSEDE_MYPROXY._value.equalsIgnoreCase(typeName) || XSEDE_MYPROXY_ALT._value.equalsIgnoreCase(typeName)) {
				return XSEDE_MYPROXY;
			} else if (LRZ_MYPROXY._value.equalsIgnoreCase(typeName) || LRZ_MYPROXY_ALT._value.equalsIgnoreCase(typeName)) {
				return LRZ_MYPROXY;
			}
			return null;
		}

		public static String[] getAllValues()
		{
			String[] toReturn = new String[3];
			toReturn[0] = NO_PROXY.getValue();
			toReturn[1] = XSEDE_MYPROXY.getValue();
			toReturn[2] = LRZ_MYPROXY.getValue();
			return toReturn;
		}
	}

	IDPLoginPanel(String Title)
	{
		setName(Title);

		// add the drop down box with the types of proxies in it.
		add(new JLabel("Proxy Setting"), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		add(_comboBox, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
			new Insets(5, 5, 5, 5), 5, 5));

		// crank up a config file for our settings.
		_loginSettings =
			new UserConfigurationFile(new File(ConfigurationManager.getCurrentConfiguration().getUserDirectory(),
				USER_SECURITY_PROPERTIES_FILE).getAbsolutePath());

		String lastProxySetting = _loginSettings.getProperty(LOGIN_PROXY_SETTING);
		if (lastProxySetting != null) {
			_type = ProxyTypes.parseString(lastProxySetting);
			if (_type != null) {
				_logger.debug("got idp login proxy type from config file of: " + _type.toString());

			} else {
				// fall back to looking at grid config.
				lastProxySetting = null;
			}
		}
		// handle when there was no previous setting (or we had an issue figuring out last value).
		if (lastProxySetting == null) {
			// no previous proxy setting, so read grid deployment to pick right one.
			String gridName = InstallationProperties.getSimpleGridName(); 
			if (gridName.equalsIgnoreCase("xcg")) {
				_type = ProxyTypes.NO_PROXY;
			} else if (gridName.equalsIgnoreCase("xsede")) {
				_type = ProxyTypes.XSEDE_MYPROXY;
			} else if (gridName.equalsIgnoreCase("gffs_eu")) {
				_type = ProxyTypes.LRZ_MYPROXY;
			} else {
				// unknown grid, so start with no proxy.
				_type = ProxyTypes.NO_PROXY;
			}
		}

		setupAccordingToType(_type);

		_comboBox.addActionListener(new InternalComboboxActionListener());
		_username.addCaretListener(new InternalUsernameCaretListener());
		_rnsPath.addCaretListener(new InternalRnspathCaretListener());

		add(new JLabel("Username"), new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		add(_username, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
			new Insets(5, 5, 5, 5), 5, 5));
		add(new JLabel("Password"), new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		add(_password, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
			new Insets(5, 5, 5, 5), 5, 5));

		add(new JLabel("Grid Path"), new GridBagConstraints(0, 3, 1, 1, 0.0, 1.0, GridBagConstraints.NORTHWEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		add(_rnsPath, new GridBagConstraints(1, 3, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
			new Insets(5, 5, 5, 5), 5, 5));
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
				JOptionPane.showMessageDialog(this, "No such user: grid path doesn't exist!  (" + _rnsPath.getText() + ")",
					"Unknown User Path", JOptionPane.ERROR_MESSAGE);
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

					if (_logger.isDebugEnabled())
						_logger.debug("before reloading have cert: " + clientKeyMaterial._clientCertChain[0].getSubjectDN());

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

				if (_logger.isDebugEnabled())
					_logger.debug("about to do idplogin with cert: " + clientKeyMaterial._clientCertChain[0].getSubjectDN());

				// login to the STS path that we've been given and see if our credentials allow it.
				ArrayList<NuCredential> creds =
					IDPLoginTool.doIdpLogin(epr, SecurityConstants.CredentialExpirationMillis,
						clientKeyMaterial._clientCertChain);

				// reload the context now, which is crucial for this method to see the most recent.
				StreamUtils.close(assumedContextToken);
				context = ContextManager.getCurrentContext();

				/*
				 * try to leave the user in the right current directory. Changed by ASAG March 6,
				 * 2014
				 */
				{
					// Assumption is that user idp's are off /user and homes off /home
					String userHome = _rnsPath.getText().replaceFirst("users", "home");

					if (_logger.isDebugEnabled()) {
						_logger.debug("guessing user home directory from sts gets: '" + userHome + "'");
					}

					try {
						CdTool.chdir(userHome);
					} catch (Throwable e) {
						_logger.debug("problem seen jumping to user home after login", e);
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

	public void setupAccordingToType(ProxyTypes newType)
	{
		if (newType != null) {
			// patch the paths to be the expected locations for xsede or gffs.eu.
			_type = newType;
			_comboBox.setEnabled(false);

			// record the last setting the user or whomever chose.
			_loginSettings.setProperty(LOGIN_PROXY_SETTING, _type.toString());

			if (newType.equals(ProxyTypes.LRZ_MYPROXY)) {
				_currentStsPath = DEFAULT_LRZ_USER_PATH;
				_proxyPort = "7512";
				_proxyHost = "myproxy.lrz.de";
				_comboBox.setSelectedIndex(2);
			} else if (newType.equals(ProxyTypes.XSEDE_MYPROXY)) {
				_currentStsPath = DEFAULT_MYPROXY_USER_PATH;
				_proxyPort = "7512";
				_proxyHost = "myproxy.xsede.org";
				_comboBox.setSelectedIndex(1);
			} else if (newType.equals(ProxyTypes.NO_PROXY)) {
				_currentStsPath = _nsd.getUsersDirectory();
				_proxyPort = null;
				_proxyHost = null;
				_comboBox.setSelectedIndex(0);
			}
		}
		// reenable combo box after possible manipulation above.
		_comboBox.setEnabled(true);
		if (_currentStsPath != null) {
			String user = _username.getText();
			if (user == null)
				user = "";
			// turn off to avoid firing an event on this change.
			_rnsPath.setEnabled(false);
			_rnsPath.setText(_currentStsPath + "/" + _username.getText());
			_rnsPath.setEnabled(true);
		}
	}

	/**
	 * handles changes to the login "proxy" setting drop down box.
	 */
	private class InternalComboboxActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			@SuppressWarnings("unchecked")
			JComboBox<String> cb = (JComboBox<String>) e.getSource();
			if ((cb != null) && cb.isEnabled()) {
				String proxy = (String) cb.getSelectedItem();
				ProxyTypes newType = ProxyTypes.parseString(proxy);
				setupAccordingToType(newType);
			}
		}
	}

	/**
	 * handles change events on the username field.
	 */
	private class InternalUsernameCaretListener implements CaretListener
	{
		@Override
		final public void caretUpdate(CaretEvent e)
		{
			if (_username.isEnabled()) {
				_rnsPath.setEnabled(false);
				_rnsPath.setText(_currentStsPath + "/" + _username.getText());
				_rnsPath.setEnabled(true);
			}
		}
	}

	/**
	 * handles events on the RNS path, which is the combination of the username and the last sts
	 * location, but which the user can also edit to use any STS.
	 */
	private class InternalRnspathCaretListener implements CaretListener
	{
		@Override
		final public void caretUpdate(CaretEvent e)
		{
			// react to control changes only if enabled to avoid firing when class changes data.
			if (_rnsPath.isEnabled()) {
				// get the current full path information from the UI.
				String path = _rnsPath.getText();
				// make sure we're seeing the same thing here, and that it makes sense.
				if ((path.length() > 1) && (_username.getText().length() > 1) && path.endsWith(_username.getText())) {
					String parent = path.substring(0, path.length() - _username.getText().length());
					if ((parent.length() > 1) && (parent.endsWith("/"))) {
						// also drop a trailing slash if the parent path isn't just root ('/').
						parent = parent.substring(0, parent.length() - 1);
					}

					_logger.debug("RESETTING THE CURRENT STS PATH TO: '" + parent + "'");
					_currentStsPath = parent;
				}
			}
		}
	}
}
