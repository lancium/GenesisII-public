package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.Properties;
import java.util.logging.Logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.uiuc.ncsa.myproxy.MyProxyLogon;
import edu.uiuc.ncsa.security.core.util.MyLoggingFacade;
import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ReloadShellException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.comm.ClientUtils;
import edu.virginia.vcgr.genii.client.configuration.Deployment;
import edu.virginia.vcgr.genii.client.configuration.DeploymentName;
import edu.virginia.vcgr.genii.client.configuration.Installation;
import edu.virginia.vcgr.genii.client.configuration.InvalidDeploymentException;
import edu.virginia.vcgr.genii.client.configuration.KeystoreSecurityConstants;
import edu.virginia.vcgr.genii.client.configuration.Security;
import edu.virginia.vcgr.genii.client.context.CallingContextImpl;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.dialog.DialogException;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.rcreate.CreationException;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.security.PreferredIdentity;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.client.utils.units.Duration;
import edu.virginia.vcgr.genii.client.utils.units.DurationUnits;
import edu.virginia.vcgr.genii.context.ContextType;
import edu.virginia.vcgr.genii.security.x509.KeyAndCertMaterial;

public class MyProxyLoginTool extends BaseLoginTool
{
	private static Log _logger = LogFactory.getLog(MyProxyLoginTool.class);

	static private final String _DESCRIPTION = "config/tooldocs/description/dMyProxyLogin";
	static private final String _USAGE_RESOURCE = "config/tooldocs/usage/uMyProxyLogin";

	static private final String MYPROXY_PROPERTIES_FILENAME = "myproxy.properties";

	static public final String MYPROXY_PORT_PROP = "edu.virginia.vcgr.genii.client.myproxy.port";
	static public final String MYPROXY_HOST_PROP = "edu.virginia.vcgr.genii.client.myproxy.host";
	static public final String MYPROXY_LIFETIME_PROP = "edu.virginia.vcgr.genii.client.myproxy.lifetime";

	private String _host = null;
	private Integer _port = 0;

	protected MyProxyLoginTool(String description, String usage, boolean isHidden)
	{
		super(description, usage, isHidden);
	}

	public MyProxyLoginTool()
	{
		super(_DESCRIPTION, _USAGE_RESOURCE, false);
		overrideCategory(ToolCategory.SECURITY);
	}

	@Option({ "host", "h" })
	public void setHost(String host)
	{
		_host = host;
	}

	@Option({ "port", "p" })
	public void setPort(String port)
	{
		_port = Integer.parseInt(port);
	}

	@Override
	protected void verify() throws ToolException
	{
		if ((_username == null) || (_username.length() == 0))
			throw new InvalidToolUsageException("The username cannot be blank.");

		if (_durationString != null) {
			try {
				_credentialValidMillis = (long) new Duration(_durationString).as(DurationUnits.Milliseconds);
			} catch (IllegalArgumentException pe) {
				throw new ToolException("Invalid duration string given.", pe);
			}
		}
	}

	static private Security getSecurityProperties()
	{
		return Installation.getDeployment(new DeploymentName()).security();
	}
	
	/**
	 * assumes that the username and password have already been set.
	 */
	public int doMyproxyLogin(ICallingContext callContext) throws ReloadShellException, ToolException, UserCancelException, RNSException,
		AuthZSecurityException, IOException, ResourcePropertyException, CreationException, InvalidToolUsageException,
		ClassNotFoundException, DialogException
	{		
		String pass = new String(_password);

		Logger jdkLogger = Logger.getLogger(this.getClass().getName());
		if (jdkLogger == null) {
			_logger.debug("jdk logger is null; will be a problem for constructing logging facade.");
		} else {
			jdkLogger.setUseParentHandlers(false);
		}

		MyLoggingFacade facade = new MyLoggingFacade(jdkLogger);
		MyProxyLogon mp = new MyProxyLogon(facade);

		Properties myProxyProperties = loadMyProxyProperties();
		int lifetime = Integer.parseInt(myProxyProperties.getProperty(MYPROXY_LIFETIME_PROP));
		
		if ((_host != null) && (_port != 0)) {			
			mp.setPort(_port);
			mp.setHost(_host);
			_logger.debug("plugging in chosen parameters for host and port: host=" + _host + " port=" + _port);
		} else {
			// Load values from properties file
			int port = Integer.parseInt(myProxyProperties.getProperty(MYPROXY_PORT_PROP));
			String host = myProxyProperties.getProperty(MYPROXY_HOST_PROP);

			mp.setPort(port);
			mp.setHost(host);			
		}

		mp.setLifetime(lifetime);
		mp.setUsername(_username);
		mp.setPassphrase(pass);

		/*
		 * Myproxy trust root can be overriden with either environment variable GLOBUS_LOCATION or X509_CERT_DIR,
		 * although at our level we are defining the location in the security property file.
		 */
		String myProxyDirectory =
			getSecurityProperties().getProperty(KeystoreSecurityConstants.Client.MYPROXY_CERTIFICATES_LOCATION_PROP);
		// default to old location if the property is not set.
		if (myProxyDirectory == null)
			myProxyDirectory = "myproxy-certs";
		File trustRoot = getSecurityProperties().getSecurityFile(myProxyDirectory);
		_logger.debug("resolved myproxy-certs folder as: " + trustRoot);

		// Set trust root.
		System.setProperty("X509_CERT_DIR", trustRoot.getCanonicalPath());

		if (_logger.isDebugEnabled())
			_logger.debug("myproxy login using host " + mp.getHost() + " and port " + mp.getPort());
		
		try {
			mp.connect();
		} catch (Exception e) {
			String msg = "Unable to connect to myproxy server: ";
			throw new AuthZSecurityException(msg + e.getLocalizedMessage(), e);
		}

		try {
			mp.logon();
			mp.getCredentials();
			mp.disconnect();
		} catch (GeneralSecurityException e) {
			throw new AuthZSecurityException("myproxy logon process got an exception: " + e.getLocalizedMessage(), e);
		}

		/*
		 * clear any existing credentials because we want to replace our session cert with the
		 * myproxy cert.
		 */
		ClientUtils.invalidateCredentials(callContext);

		// reset any previous pass-through credential.
		callContext.removeProperty(GenesisIIConstants.PASS_THROUGH_IDENTITY);
		ContextManager.storeCurrentContext(callContext);

		X509Certificate[] keyMat = new X509Certificate[1];
		keyMat[0] = mp.getCertificate();

		String msg =
			"Replacing client tool identity with MyProxy credentials for \"" + keyMat[0].getSubjectDN().getName() + "\".";
		stdout.println(msg);
		_logger.info(msg);

		// store the new myproxy session cert.
		KeyAndCertMaterial clientKeyMaterial = new KeyAndCertMaterial(keyMat, mp.getPrivateKey());
		callContext.setActiveKeyAndCertMaterial(clientKeyMaterial);
		
		// add the pass through identity to the calling context.
		callContext.setSingleValueProperty(GenesisIIConstants.PASS_THROUGH_IDENTITY, keyMat[0]);
		if (_logger.isDebugEnabled())
			_logger.debug("issuer for the new cert is: " + keyMat[0].getIssuerDN());
		if (_logger.isTraceEnabled())
			_logger.trace("adding myproxy TLS cert: " + keyMat[0]);		
		
		// handle establishing a preferred identity, if that's not set as fixated.
		if (!PreferredIdentity.fixatedInCurrent()) {
			// the identity wasn't fixated, or it doesn't exist, so we can set preferred identity here.
			PreferredIdentity newIdentity = new PreferredIdentity(PreferredIdentity.getDnString(keyMat[0]), false);
			PreferredIdentity.setInContext(callContext, newIdentity);
			_logger.debug("preferred identity set in myproxy login: " + newIdentity.toString());
		}				
		
		// update the saved context before we leave.
		ContextManager.storeCurrentContext(callContext);
		return 0;
	}
	
	@Override
	protected int runCommand() throws ReloadShellException, ToolException, UserCancelException, RNSException,
		AuthZSecurityException, IOException, ResourcePropertyException, CreationException, InvalidToolUsageException,
		ClassNotFoundException, DialogException
	{
		// make sure username/password are set
		aquireUsername();
		aquirePassword();

		ICallingContext callContext = ContextManager.getCurrentContext();
		if (callContext == null)
			callContext = new CallingContextImpl(new ContextType());

		int toReturn = doMyproxyLogin(callContext);

		return toReturn;
	}

	static private Properties loadMyProxyProperties()
	{
		FileInputStream fin = null;
		Properties ret = new Properties();

		Deployment deployment = Installation.getDeployment(new DeploymentName());
		try {

			fin = new FileInputStream(deployment.getConfigurationFile(MYPROXY_PROPERTIES_FILENAME));
			ret.load(fin);
			return ret;
		} catch (IOException ioe) {
			throw new InvalidDeploymentException(deployment.getName().toString(),
				"Unable to load myproxy  properties from deployment.");
		} finally {
			StreamUtils.close(fin);
		}
	}

}
