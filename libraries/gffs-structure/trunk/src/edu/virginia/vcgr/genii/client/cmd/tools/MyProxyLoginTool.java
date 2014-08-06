package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.io.StreamUtils;

import edu.uiuc.ncsa.MyProxy.MyProxyLogon;
import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ReloadShellException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.configuration.Deployment;
import edu.virginia.vcgr.genii.client.configuration.DeploymentName;
import edu.virginia.vcgr.genii.client.configuration.Installation;
import edu.virginia.vcgr.genii.client.configuration.InvalidDeploymentException;
import edu.virginia.vcgr.genii.client.context.CallingContextImpl;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.dialog.DialogException;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.rcreate.CreationException;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.client.utils.units.Duration;
import edu.virginia.vcgr.genii.client.utils.units.DurationUnits;
import edu.virginia.vcgr.genii.context.ContextType;
import edu.virginia.vcgr.genii.security.TransientCredentials;
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
	private Boolean _myProxyHost = false;

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
	public void setName(String host)
	{
		_host = host;
		_myProxyHost = true;
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

	@Override
	protected int runCommand() throws ReloadShellException, ToolException, UserCancelException, RNSException,
		AuthZSecurityException, IOException, ResourcePropertyException, CreationException, InvalidToolUsageException,
		ClassNotFoundException, DialogException
	{
		// make sure username/password are set
		aquireUsername();
		aquirePassword();

		String pass = new String(_password);

		MyProxyLogon mp = new MyProxyLogon();

		// Load properties file
		Properties myProxyProperties = loadMyProxyProperties();

		int port = Integer.parseInt(myProxyProperties.getProperty(MYPROXY_PORT_PROP));

		String host = myProxyProperties.getProperty(MYPROXY_HOST_PROP);

		int lifetime = Integer.parseInt(myProxyProperties.getProperty(MYPROXY_LIFETIME_PROP));

		mp.setPort(port);
		mp.setHost(host);
		if (_myProxyHost && (_port != 0)) {
			mp.setPort(_port);
			mp.setHost(_host);
		}

		mp.setLifetime(lifetime);
		mp.setUsername(_username);
		mp.setPassphrase(pass);

		/*
		 * Myproxy trust root most likely want to make configurable in the future Can be overriden
		 * with either environment variable GLOBUS_LOCATION or X509_CERT_DIR
		 */

		/*
		 * hmmm: this should use a configured name, which would allow us to store this anywhere!
		 * that will be a requirement if we are to use an absolute path as desired by xsede.
		 */
		File trustRoot = Installation.getDeployment(new DeploymentName()).security().getSecurityFile("myproxy-certs");
		// = new File(ContainerProperties.getContainerProperties().getDeploymentsDirectory() + "/"
		// + Installation.getDeployment(new DeploymentName()).getName().toString() +
		// "/security/myproxy-certs/");

		_logger.debug("resolved myproxy-certs folder as: " + trustRoot);

		// Set trust root.
		System.setProperty("X509_CERT_DIR", trustRoot.getCanonicalPath());

		try {
			mp.connect();
		} catch (Exception e) {
			stdout.println("Unable to login via myproxy");
		}

		try {
			mp.logon();
			mp.getCredentials();
			mp.disconnect();
		} catch (GeneralSecurityException e) {
			throw new AuthZSecurityException("myproxy logon process got an exception: " + e.getLocalizedMessage(), e);
		}

		// get the local identity's key material (or create one if necessary)
		ICallingContext callContext = ContextManager.getCurrentContext();
		if (callContext == null)
			callContext = new CallingContextImpl(new ContextType());

		TransientCredentials.globalLogout(callContext);

		X509Certificate[] keyMat = new X509Certificate[1];
		keyMat[0] = mp.getCertificate();

		String msg =
			"Replacing client tool identity with MyProxy credentials for \"" + keyMat[0].getSubjectDN().getName() + "\".";
		stdout.println(msg);
		_logger.info(msg);

		KeyAndCertMaterial clientKeyMaterial = new KeyAndCertMaterial(keyMat, mp.getPrivateKey());
		callContext.setActiveKeyAndCertMaterial(clientKeyMaterial);

		// add the pass through identity to the calling context.
		callContext.setSingleValueProperty(GenesisIIConstants.PASS_THROUGH_IDENTITY, keyMat[0]);

		// update the saved context before we leave.
		ContextManager.storeCurrentContext(callContext);

		return 0;
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
