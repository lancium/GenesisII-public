package edu.virginia.vcgr.genii.container;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.*;

import javax.servlet.ServletException;

import org.apache.axis.AxisFault;
import org.apache.axis.EngineConfiguration;
import org.apache.axis.MessageContext;
import org.apache.axis.SimpleChain;
import org.apache.axis.deployment.wsdd.WSDDProvider;
import org.apache.axis.description.JavaServiceDesc;
import org.apache.axis.server.AxisServer;
import org.apache.axis.transport.http.AxisServletBase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.configuration.XMLConfiguration;
import org.mortbay.http.HttpContext;
import org.mortbay.http.SocketListener;
import org.mortbay.http.SslListener;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.servlet.WebApplicationContext;

import edu.virginia.vcgr.genii.client.ApplicationBase;
import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.configuration.Hostname;
import edu.virginia.vcgr.genii.client.install.InstallationState;
import edu.virginia.vcgr.genii.client.security.x509.CertTool;
import edu.virginia.vcgr.genii.client.stats.ContainerStatistics;
import edu.virginia.vcgr.genii.client.utils.deployment.DeploymentRelativeFile;
import edu.virginia.vcgr.genii.client.utils.flock.FileLockException;
import edu.virginia.vcgr.genii.container.configuration.ContainerConfiguration;
import edu.virginia.vcgr.genii.container.deployment.ServiceDeployer;
import edu.virginia.vcgr.genii.container.invoker.GAroundInvokerFactory;
import edu.virginia.vcgr.genii.container.axis.ServerWSDoAllReceiver;
import edu.virginia.vcgr.genii.container.axis.ServerWSDoAllSender;

public class Container extends ApplicationBase
{
	static private Log _logger = LogFactory.getLog(Container.class);
	
	static private AxisServer _axisServer = null;
	static private ContainerConfiguration _containerConfiguration;
	
	/* MOOCH
	static private EventManager _eventManager;
	static private AlarmManager _alarmManager;
	static private LifetimeVulture _vulture;
	*/
	static private String _containerURL;
	
	static private X509Certificate[] _containerCertChain;
	static private PrivateKey _containerPrivateKey;
	
		// Default to 1 year
	static private long _defaultCertificateLifetime = 1000L * 60L * 60L * 24L * 365L;
	
	static public void usage() {
		System.out.println("Container [deployment-name]");
	}
	
	static public void main(String []args)
	{	
		if (args.length > 1)
		{
			usage();
			System.exit(1);
		}
		
		ContainerStatistics.instance();
		
		if (args.length == 1) {
			System.setProperty(GenesisIIConstants.DEPLOYMENT_NAME_PROPERTY, args[0]);
			_logger.info("Container deployment is " + args[0]);
		} 
		else {
			System.setProperty(GenesisIIConstants.DEPLOYMENT_NAME_PROPERTY, "default");
			_logger.info("Container deployment is default");
		}
		
		prepareServerApplication();
		
		try
		{
// MOOCH			_eventManager = new EventManager();
// MOOCH			_alarmManager = new AlarmManager(_eventManager);
// MOOCH			_vulture = new LifetimeVulture(_eventManager, _alarmManager);
			
			WSDDProvider.registerProvider(
				GAroundInvokerFactory.PROVIDER_QNAME,
				new GAroundInvokerFactory());

			runContainer();
			System.out.println("Container Started");
		}
		catch (Throwable t)
		{
			t.printStackTrace(System.err);
			System.exit(1);
		}
	}
	
	static public ConfigurationManager getConfigurationManager()
	{
		return ConfigurationManager.getCurrentConfiguration();
	}
	
	static public ContainerConfiguration getContainerConfiguration()
	{
		return _containerConfiguration;
	}
	
	/* MOOCH
	static public LifetimeVulture getLifetimeVulture()
	{
		return _vulture;
	}
	*/
	
	static private org.apache.axis.Handler getHandler(
			SimpleChain handlerChain, 
			Class<?> handlerClass) {

		if (handlerChain == null) {
			return null;
		}
		for (org.apache.axis.Handler h : handlerChain.getHandlers()) {
			if (h instanceof SimpleChain) {
				org.apache.axis.Handler result = 
					getHandler((SimpleChain) h, handlerClass);
				if (result != null) {
					return result;
				}
			} else if (h.getClass().equals(handlerClass)) {
				return h;
			}
		}
		return null;
	}
	
	
	static private void runContainer()
		throws ConfigurationException, IOException, Exception
	{
		WebApplicationContext webAppCtxt;
		Server server;
		SocketListener listener;
		
		initializeIdentitySecurity(getConfigurationManager().getContainerConfiguration());

		_containerConfiguration = new ContainerConfiguration(getConfigurationManager());
		
		server = new Server();
		
		if (_containerConfiguration.isSSL())
		{
			listener = new SslListener();
			listener.setPort(_containerConfiguration.getListenPort());
			_containerConfiguration.getSslInformation().configure(getConfigurationManager(),
				(SslListener)listener);
			_containerURL = Hostname.normalizeURL(
				"https://127.0.0.1:" + _containerConfiguration.getListenPort());
		} else
		{
			listener = new SocketListener();
			listener.setPort(_containerConfiguration.getListenPort());
			_containerURL = Hostname.normalizeURL(
				"http://127.0.0.1:" + _containerConfiguration.getListenPort());
		}
		server.addListener(listener);
		
		HttpContext context = new HttpContext();
		context.setContextPath("/");
		context.addHandler(new ResourceFileHandler("edu/virginia/vcgr/genii/container"));
		server.addContext(context);
		
		webAppCtxt = server.addWebApplication(
				"/axis",
				new File(ConfigurationManager.getInstallDir(),"webapps/axis").getAbsolutePath());
		
		recordInstallationState(System.getProperty(
			GenesisIIConstants.DEPLOYMENT_NAME_PROPERTY, "default"), 
			new URL(_containerURL));
		
		server.start();
		initializeServices(webAppCtxt);
	
		// This line was checked in and clearly breaks the system.
		//
		// ServiceDeployer.startServiceDeployer(_axisServer,
		//	new File(getConfigurationManager().getConfigDirectory(), "services"));
		ServiceDeployer.startServiceDeployer(_axisServer,
			new DeploymentRelativeFile("services"));
	}
	
	static private void initializeServices(WebApplicationContext ctxt)
		throws ServletException, AxisFault
	{
		ServletHolder []holders = ctxt.getServletHandler().getServlets();
		for (ServletHolder holder : holders)
		{
			if (holder.getName().equals("AxisServlet"))
			{
				AxisServletBase s = (AxisServletBase)holder.getServlet();
				_axisServer = s.getEngine();
			}
		}
		
		if (_axisServer == null)
			throw new AxisFault("Internal error trying to start container.");
		
		_axisServer.setShouldSaveConfig(false);
		
		try
		{
        	EngineConfiguration config = _axisServer.getConfig();
        	
        	// configure the listening request security handler
        	ServerWSDoAllReceiver receiver = 
        		(ServerWSDoAllReceiver) getHandler((SimpleChain) config.getGlobalRequest(), ServerWSDoAllReceiver.class);
        	receiver.configure(_containerPrivateKey);
        	
        	// configure listening response security handler
        	ServerWSDoAllSender sender = 
        		(ServerWSDoAllSender) getHandler((SimpleChain) config.getGlobalResponse(), ServerWSDoAllSender.class);
        	sender.configure(_containerPrivateKey);
			
			// configure the services individually
        	Iterator<?> iter = _axisServer.getConfig().getDeployedServices();
			while (iter.hasNext())
			{
				Object obj = iter.next();
				if (obj instanceof JavaServiceDesc)
				{
					Class<?> implClass = ((JavaServiceDesc)obj).getImplClass();
					if (IContainerManaged.class.isAssignableFrom(implClass))
					{
						Constructor<?> cons = implClass.getConstructor(new Class[0]);
						IContainerManaged base =
							(IContainerManaged)cons.newInstance(new Object[0]);
						base.startup();
					}
				}
			}
		}
		catch (InstantiationException ie)
		{
			throw new AxisFault(ie.getLocalizedMessage(), ie);
		}
		catch (InvocationTargetException ite)
		{
			Throwable t = ite.getCause();
			if (t == null)
				t = ite;
			throw new AxisFault(t.getLocalizedMessage(), t);
		}
		catch (IllegalAccessException iae)
		{
			throw new AxisFault(iae.getLocalizedMessage(), iae);
		}
		catch (NoSuchMethodException nsme)
		{
			throw new AxisFault(nsme.getLocalizedMessage(), nsme);
		}
		catch (org.apache.axis.ConfigurationException ce)
		{
			throw new AxisFault(ce.getLocalizedMessage(), ce);
		}
	}
	
	static private void initializeIdentitySecurity(XMLConfiguration serverConf)
		throws ConfigurationException, KeyStoreException, 
			GeneralSecurityException, IOException
	{
		Properties resourceIdSecProps = null;
		try
		{
			resourceIdSecProps = (Properties)serverConf.retrieveSection(
				GenesisIIConstants.RESOURCE_IDENTITY_PROPERTIES_SECTION_NAME);
		}
		catch (ConfigurationException ce)
		{
			return;
		}
		
		String keyStoreLoc = resourceIdSecProps.getProperty(
			"edu.virginia.vcgr.genii.container.security.resource-identity.key-store");
		String keyStoreType = resourceIdSecProps.getProperty(
			"edu.virginia.vcgr.genii.container.security.resource-identity.key-store-type", "PKCS12");
		String keyPassword = resourceIdSecProps.getProperty(
			"edu.virginia.vcgr.genii.container.security.resource-identity.key-password");
		String keyStorePassword = resourceIdSecProps.getProperty(
			"edu.virginia.vcgr.genii.container.security.resource-identity.key-store-password");
		String containerAlias = resourceIdSecProps.getProperty(
			"edu.virginia.vcgr.genii.container.security.resource-identity.container-alias", 
			GenesisIIConstants.CONTAINER_CERT_ALIAS);
		
		String certificateLifetime = resourceIdSecProps.getProperty(
			"edu.virginia.vcgr.genii.container.security.resource-identity.default-certificate-lifetime");
		if (certificateLifetime != null)
			_defaultCertificateLifetime = Long.parseLong(certificateLifetime);
		
		if (keyStoreLoc == null)
			throw new ConfigurationException(
				"Key Store Location not specified for message security.");
		
		
		// open the keystore
		char[] keyStorePassChars = null;
		if (keyStorePassword != null)
			keyStorePassChars = keyStorePassword.toCharArray();
		char[] keyPassChars = null;
		if (keyPassword != null)
			keyPassChars = keyPassword.toCharArray();

		KeyStore ks = CertTool.openStoreDirectPath(new DeploymentRelativeFile(keyStoreLoc),
			keyStoreType, keyStorePassChars);
		// load the container private key and certificate
		_containerPrivateKey = (PrivateKey) ks.getKey(
				containerAlias, keyPassChars);
			    
		Certificate[] chain = ks.getCertificateChain(
				containerAlias);
		_containerCertChain = new X509Certificate[chain.length];
		for (int i = 0; i < chain.length; i++)
			_containerCertChain[i] = (X509Certificate) chain[i];
		
	}
	
	static public ArrayList<JavaServiceDesc> getInstalledServices()
	{
		ArrayList<JavaServiceDesc> installedServices = 
			new ArrayList<JavaServiceDesc>();
		
		Iterator<?> iter = null;
		try
		{
        	iter = _axisServer.getConfig().getDeployedServices();
        
	        while (iter.hasNext())
	        {
	        	Object obj = iter.next();
	        	if (obj instanceof JavaServiceDesc)
	        		installedServices.add((JavaServiceDesc)obj);
	        }
		}
		catch (org.apache.axis.ConfigurationException ce)
		{
			_logger.info(ce.getLocalizedMessage(), ce);
		}
		
		return installedServices;
	}
	
	static public String getServiceURL(String serviceName)
	{
		MessageContext ctxt = MessageContext.getCurrentContext();
		if (ctxt != null)
		{
			String currentURL = getCurrentServiceURL(ctxt);
			int index = currentURL.lastIndexOf('/');
			if (index > 0)
				return currentURL.substring(0, index + 1) + serviceName;
		}
		
		return _containerURL + "/axis/services/" + serviceName;
	}
	
	static public String getCurrentServiceURL(MessageContext ctxt)
	{
		try
		{
			URL url = new URL(ctxt.getProperty(
				MessageContext.TRANS_URL).toString());
			URL result = new URL(url.getProtocol(),
				Hostname.getLocalHostname().toString(), url.getPort(),
				url.getFile());
			return result.toString();
		}
		catch (MalformedURLException mue)
		{
			// Can't happen
			_logger.fatal("This shouldn't have happend:  " + mue);
			throw new RuntimeException(mue);
		}
	}
	
	static public X509Certificate[] getContainerCertChain() 
	{
		return _containerCertChain;
	}

	static public PrivateKey getContainerPrivateKey() 
	{
		return _containerPrivateKey;
	}
	
	static public long getDefaultCertificateLifetime()
	{
		return _defaultCertificateLifetime;
	}
	
	static private void recordInstallationState(String deploymentName, URL containerURL)
		throws IOException, FileLockException
	{
		Thread th = new Thread(new InstallationStateEraser(deploymentName));
		th.setDaemon(false);
		th.setName("Installation Eraser Thread");
		Runtime.getRuntime().addShutdownHook(th);
		InstallationState.addRunningContainer(deploymentName, containerURL);
	}
	
	static private class InstallationStateEraser implements Runnable
	{
		private String _deploymentName;
		
		public InstallationStateEraser(String deploymentName)
		{
			_deploymentName = deploymentName;
		}
		
		@Override
		public void run()
		{
			try
			{
				InstallationState.removeRunningContainer(_deploymentName);
			}
			catch (Throwable cause)
			{
				_logger.fatal("Unable to remove container state.", cause);
			}
		}
	}
}
