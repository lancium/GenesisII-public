package edu.virginia.vcgr.genii.ui.login;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Closeable;
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
	static private Log _logger = LogFactory.getLog(IDPLoginPanel.class);

	final public String DEFAULT_LRZ_USER_PATH = "/users/gffs.eu/lrz.de";
	final public String DEFAULT_MYPROXY_USER_PATH = "/users/xsede.org";
	
	private String _currentStsPath = ""; 
	private JTextField _username = new JTextField(16);
	private JTextField _rnsPath = new JTextField(64);
	private JPasswordField _password = new JPasswordField(16);
	private ProxyTypes _type = ProxyTypes.NO_PROXY;
	private JComboBox<String> _comboBox = new JComboBox<String>(ProxyTypes.getAllValues());

	private String _proxyPort = null;
	private String _proxyHost = null;

	NamespaceDefinitions nsd = Installation.getDeployment(new DeploymentName()).namespace();
	
	// the kinds of proxies we know about.
	public enum ProxyTypes {
		NO_PROXY("No Proxy"),
		XSEDE_MYPROXY("XSEDE MyProxy"),
		LRZ_MYPROXY("LRZ MyProxy");
		
		String _value = null;
		
		ProxyTypes(String value) {
			_value = value;
		}
		
		public String getValue() {
			return _value;
		}
		
		public static ProxyTypes parseString(String typeName)
		{
			if (NO_PROXY._value.equals(typeName)) {
				return NO_PROXY;
			} else if (XSEDE_MYPROXY._value.equals(typeName)) {
				return XSEDE_MYPROXY;				
			} else if (LRZ_MYPROXY._value.equals(typeName)) {
				return LRZ_MYPROXY;
			}
			return null;
		}
		
		public static String[] getAllValues() {
			String [] toReturn = new String[3];
			toReturn[0] = NO_PROXY.getValue();
			toReturn[1] = XSEDE_MYPROXY.getValue();
			toReturn[2] = LRZ_MYPROXY.getValue();
			return toReturn;
		}
	}

	IDPLoginPanel(String Title)
	{
		setName(Title);

		_username.addCaretListener(new InternalCaretListener());
		
		// add the drop down box with the types of proxies in it.
		add(new JLabel("Proxy Setting"), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		add(_comboBox, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));

		//hmmm: need to set based on grid!
		_type = ProxyTypes.XSEDE_MYPROXY;
		setupAccordingToType(_type);
		// 0 is none, 1 is xsede, 2 is lrz, index must match setting above!
		_comboBox.setSelectedIndex(1);
		
		// hmmm: also need to record the type in user prefs!!!
		
		_comboBox.addActionListener(new InternalActionListener());		
		
		add(new JLabel("Username"), new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		add(_username, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
			new Insets(5, 5, 5, 5), 5, 5));
		add(new JLabel("Password"), new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
		add(_password, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
			new Insets(5, 5, 5, 5), 5, 5));
		
//		if ((_type != ProxyTypes.XSEDE_MYPROXY) && (_type != ProxyTypes.LRZ_MYPROXY)) {
			add(new JLabel("Grid Path"), new GridBagConstraints(0, 3, 1, 1, 0.0, 1.0, GridBagConstraints.NORTHWEST,
				GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
			add(_rnsPath, new GridBagConstraints(1, 3, 1, 1, 1.0, 1.0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL,
				new Insets(5, 5, 5, 5), 5, 5));
//		}
			
			
			
			
	}

	@Override
	final public Collection<NuCredential> doLogin(UIContext uiContext) throws Throwable
	{
		Closeable assumedContextToken = null;

		try {
			ICallingContext context = uiContext.callingContext();
			assumedContextToken = ContextManager.temporarilyAssumeContext(context);

			//hmmm: these are hard-coded paths here for xsede and gffs.eu!  this needs to be fixed, and probably just use the normal path picker.
			
			String stsLocation = _rnsPath.getText();
			
			// patch the paths to be the expected locations for xsede or gffs.eu.
			if (_type.equals(ProxyTypes.LRZ_MYPROXY) ) {
				stsLocation = "/users/gffs.eu/lrz.de/" + _username.getText();
			}
			if (_type.equals(ProxyTypes.XSEDE_MYPROXY) ) {
				stsLocation = "/users/xsede.org/" + _username.getText();
			}
			
			RNSPath path = context.getCurrentPath().lookup(stsLocation, RNSPathQueryFlags.DONT_CARE);
			if (!path.exists()) {
				JOptionPane.showMessageDialog(this, "No such user: grid path doesn't exist!  (" + stsLocation + ")", "Unknown User Path",
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
			_rnsPath.setText(_currentStsPath + "/" + _username.getText());
		}
	}
	
	public void setupAccordingToType(ProxyTypes newType)
	{
		if (newType != null) {
			// patch the paths to be the expected locations for xsede or gffs.eu.
			_type = newType;
			if (newType.equals(ProxyTypes.LRZ_MYPROXY) ) {
				_currentStsPath = DEFAULT_LRZ_USER_PATH;
				_proxyPort = "7512";
				_proxyHost = "myproxy.lrz.de";
			} else if (newType.equals(ProxyTypes.XSEDE_MYPROXY) ) {
				_currentStsPath = DEFAULT_MYPROXY_USER_PATH;
				_proxyPort = "7512";
				_proxyHost = "myproxy.xsede.org";
			} else if (newType.equals(ProxyTypes.NO_PROXY)) {
				_currentStsPath = nsd.getUsersDirectory();
				_proxyPort = null;
				_proxyHost = null;
			}
		}
		if (_currentStsPath != null) {
			String user = _username.getText();
			if (user == null)
				user = "";
			_rnsPath.setText(_currentStsPath + "/" + _username.getText());
		}
	}
	
	private class InternalActionListener implements ActionListener
	{
		@Override
		public void actionPerformed(ActionEvent e)
		{
			@SuppressWarnings("unchecked")
			JComboBox<String> cb = (JComboBox<String>)e.getSource();
	        String proxy = (String)cb.getSelectedItem();
	        //updateLabel(proxy);
			
			ProxyTypes newType = ProxyTypes.parseString(proxy);
			setupAccordingToType(newType);
		}		
	}
}
