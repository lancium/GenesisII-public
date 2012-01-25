package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Properties;


import org.morgan.util.io.StreamUtils;


import edu.uiuc.ncsa.MyProxy.MyProxyLogon;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.configuration.Deployment;
import edu.virginia.vcgr.genii.client.configuration.DeploymentName;
import edu.virginia.vcgr.genii.client.configuration.Installation;
import edu.virginia.vcgr.genii.client.configuration.InvalidDeploymentException;
import edu.virginia.vcgr.genii.client.context.CallingContextImpl;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.security.credentials.TransientCredentials;
import edu.virginia.vcgr.genii.client.security.x509.KeyAndCertMaterial;
import edu.virginia.vcgr.genii.context.ContextType;

public class MyProxyLoginTool extends BaseLoginTool{


	static private final String _DESCRIPTION = "edu/virginia/vcgr/genii/client/cmd/tools/description/dMyProxyLogin";
	static private final String _USAGE_RESOURCE = 
		"edu/virginia/vcgr/genii/client/cmd/tools/usage/uMyProxyLogin";

	static private final String MYPROXY_PROPERTIES_FILENAME = 
			"myproxy.properties";
		
	static public final String MYPROXY_PORT_PROP =
			"edu.virginia.vcgr.genii.client.myproxy.port";
	static public final String MYPROXY_HOST_PROP =
			"edu.virginia.vcgr.genii.client.myproxy.host";
	static public final String MYPROXY_LIFETIME_PROP =
			"edu.virginia.vcgr.genii.client.myproxy.lifetime";

	
	protected MyProxyLoginTool(String description, String usage, boolean isHidden) {
		super(description, usage, isHidden);
	}

	public MyProxyLoginTool() {
		super(_DESCRIPTION, _USAGE_RESOURCE, false);
		overrideCategory(ToolCategory.SECURITY);
	}

	@Override
	protected void verify() throws ToolException {
		// TODO Auto-generated method stub

	}




	@Override
	protected int runCommand() throws Throwable {

	
		//make sure username/password are set
		aquireUsername();
		aquirePassword();

		String pass = new String(_password);

		MyProxyLogon mp = new MyProxyLogon();
		
		//Load properties file
		Properties myProxyProperties = loadMyProxyProperties();
	
		int port = Integer.parseInt(
				myProxyProperties.getProperty(MYPROXY_PORT_PROP));
		
		String host = myProxyProperties.getProperty(MYPROXY_HOST_PROP);
		
		int lifetime = Integer.parseInt(
				myProxyProperties.getProperty(MYPROXY_LIFETIME_PROP));
		
		mp.setPort(port);
		mp.setHost(host);
		mp.setLifetime(lifetime);
		mp.setUsername(_username);
		mp.setPassphrase(pass);	
		
		
		//Myproxy trust root
		//most likely want to make configurable in the future
		//Can be overriden with either environment variable
		//GLOBUS_LOCATION or X509_CERT_DIR
		
		File installLocation = Installation.getInstallDirectory();
		File trustRoot = new File(installLocation, 
				"/deployments/" + Installation.getDeployment(new DeploymentName()).getName().toString() +
				"/security/myproxy-certs/");
		
			
		//Set trust root
		System.setProperty("X509_CERT_DIR",
				trustRoot.getCanonicalPath());


		try{
			mp.connect();
		}
		catch(Exception e){
			stdout.println("Unable to login via myproxy");
			//e.printStackTrace();
		}
		
		mp.logon();
		mp.getCredentials();
		mp.disconnect();
		
		// get the local identity's key material (or create one if necessary)
		ICallingContext callContext = ContextManager.getCurrentContext(false);
		if (callContext == null)
			callContext = new CallingContextImpl(new ContextType());
		
	
		X509Certificate[] creds = new X509Certificate[1];
		creds[0] = mp.getCertificate();
		
		
		TransientCredentials.globalLogout(callContext);

		stdout.println("Replacing client tool identity with credentials for \""
				+ creds[0].getSubjectDN().getName() + "\".");
		

		KeyAndCertMaterial clientKeyMaterial = 
			new KeyAndCertMaterial(creds, mp.getPrivateKey());
		callContext.setActiveKeyAndCertMaterial(clientKeyMaterial);

		return 0;
	}

	
	static private Properties loadMyProxyProperties()
		{
			FileInputStream fin = null;
			Properties ret = new Properties();
			
			Deployment deployment = Installation.getDeployment(new DeploymentName());
			try
			{
					
				fin = new FileInputStream(deployment.getConfigurationFile(
						MYPROXY_PROPERTIES_FILENAME));
				ret.load(fin);
				return ret;
			}
			catch (IOException ioe)
			{
				throw new InvalidDeploymentException(deployment.getName().toString(),
					"Unable to load myproxy  properties from deployment.");
			}
			finally
			{
				StreamUtils.close(fin);
			}
		}

}
