package edu.virginia.vcgr.genii.container;

import java.io.IOException;
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
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.server.AxisServer;
import org.apache.axis.transport.http.AxisServletBase;
import org.apache.axis.types.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.GUID;
import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.configuration.XMLConfiguration;
import org.mortbay.jetty.*;
import org.mortbay.jetty.handler.*;
import org.mortbay.jetty.webapp.*;
import org.mortbay.jetty.security.SslSocketConnector;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.servlet.ServletHolder;

import org.ws.addressing.AttributedURIType;
import org.ws.addressing.EndpointReferenceType;

import edu.virginia.vcgr.genii.client.ApplicationBase;
import edu.virginia.vcgr.genii.client.GenesisIIConstants;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.configuration.DeploymentName;
import edu.virginia.vcgr.genii.client.configuration.GridEnvironment;
import edu.virginia.vcgr.genii.client.configuration.Hostname;
import edu.virginia.vcgr.genii.client.configuration.Installation;
import edu.virginia.vcgr.genii.client.configuration.Security;
import edu.virginia.vcgr.genii.client.configuration.SecurityConstants;
import edu.virginia.vcgr.genii.client.comm.jetty.TrustAllSslSocketConnector;
import edu.virginia.vcgr.genii.client.install.InstallationState;
import edu.virginia.vcgr.genii.client.naming.EPRUtils;
import edu.virginia.vcgr.genii.client.security.x509.CertTool;
import edu.virginia.vcgr.genii.client.stats.ContainerStatistics;
import edu.virginia.vcgr.genii.client.utils.flock.FileLockException;
import edu.virginia.vcgr.genii.container.configuration.ContainerConfiguration;
import edu.virginia.vcgr.genii.container.cservices.ContainerServices;
import edu.virginia.vcgr.genii.container.deployment.ServiceDeployer;
import edu.virginia.vcgr.genii.container.invoker.GAroundInvokerFactory;
import edu.virginia.vcgr.genii.container.alarms.AlarmManager;
import edu.virginia.vcgr.genii.container.axis.ServerWSDoAllReceiver;
import edu.virginia.vcgr.genii.container.axis.ServerWSDoAllSender;
import edu.virginia.vcgr.secrun.SecureRunnableHooks;
import edu.virginia.vcgr.secrun.SecureRunnerManager;

public class Container extends ApplicationBase
{
	static private Log _logger = LogFactory.getLog(Container.class);
	
	static private AxisServer _axisServer = null;
	static private ContainerConfiguration _containerConfiguration;
	
	static private String _containerURL;
	
	static private X509Certificate[] _containerCertChain;
	static private PrivateKey _containerPrivateKey;
	
		// Default to 1 year
	static private long _defaultCertificateLifetime = 1000L * 60L * 60L * 24L * 365L;
	
	static public void usage() {
		System.out.println("Container [deployment-name]");
	}
	
	static private SecureRunnerManager _secRunManager;
	
	static public void main(String []args)
	{	
		if (args.length > 1)
		{
			usage();
			System.exit(1);
		}
		
		GridEnvironment.loadGridEnvironment();
		
		ContainerStatistics.instance();
		
		if (args.length == 1)
			System.setProperty(DeploymentName.DEPLOYMENT_NAME_PROPERTY, args[0]);
		
		prepareServerApplication();
		
		_logger.info(String.format(
			"Deployment name is %s.\n", new DeploymentName()));
		_secRunManager = SecureRunnerManager.createSecureRunnerManager(
			Container.class.getClassLoader(),
			Installation.getDeployment(new DeploymentName()));
		Properties secRunProperties = new Properties();
		_secRunManager.run(SecureRunnableHooks.CONTAINER_PRE_STARTUP, 
			secRunProperties);
		
		try
		{
			WSDDProvider.registerProvider(
				GAroundInvokerFactory.PROVIDER_QNAME,
				new GAroundInvokerFactory());
			
			runContainer();
			
			System.out.println("Container Started");
			_secRunManager.run(SecureRunnableHooks.CONTAINER_POST_STARTUP, 
				secRunProperties);
			AlarmManager.initializeAlarmManager();
		}
		catch (Throwable t)
		{
			t.printStackTrace(System.err);
			System.exit(1);
		}
		/* We have decided not to do this.
		SoftwareRejuvenator.startRejuvenator();
		*/
	}
	
	static public ConfigurationManager getConfigurationManager()
	{
		return ConfigurationManager.getCurrentConfiguration();
	}
	
	static public ContainerConfiguration getContainerConfiguration()
	{
		return _containerConfiguration;
	}
	
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
		WebAppContext webAppCtxt;
		Server server;
		SocketConnector socketConnector;
		
		initializeIdentitySecurity(getConfigurationManager().getContainerConfiguration());

		_containerConfiguration = new ContainerConfiguration(getConfigurationManager());
		
		server = new Server();
		
		if (_containerConfiguration.isSSL())
		{
			socketConnector = new TrustAllSslSocketConnector();
			socketConnector.setPort(_containerConfiguration.getListenPort());
			_containerConfiguration.getSslInformation().configure(getConfigurationManager(),
				(SslSocketConnector)socketConnector);
			_containerURL = Hostname.normalizeURL(
				"https://127.0.0.1:" + _containerConfiguration.getListenPort());
		} else
		{
			socketConnector = new SocketConnector();
			socketConnector.setPort(_containerConfiguration.getListenPort());
			_containerURL = Hostname.normalizeURL(
				"http://127.0.0.1:" + _containerConfiguration.getListenPort());
		}

		_logger.info(String.format("Setting max acceptor threads to %d\n",
			_containerConfiguration.getMaxAcceptorThreads()));
		socketConnector.setAcceptors(_containerConfiguration.getMaxAcceptorThreads());
		server.addConnector(socketConnector);
		
		ContextHandler context = new ContextHandler("/axis");
		server.addHandler(context);
		webAppCtxt = new WebAppContext(
				Installation.axisWebApplicationPath().getAbsolutePath(),
				"/");
		context.addHandler(webAppCtxt);
		
		context = new ContextHandler("/");
		server.addHandler(context);
		context.addHandler(new ResourceFileHandler(
			"edu/virginia/vcgr/genii/container"));
		
		try
		{
			recordInstallationState(System.getProperty(
					DeploymentName.DEPLOYMENT_NAME_PROPERTY, "default"), 
				new URL(_containerURL));
		}
		catch (Throwable cause)
		{
			_logger.error(
				"Unable to record installation state -- continuing anyways.", 
				cause);
		}
		
		server.start();
		
		_logger.info(String.format("Container ID:  %s", getContainerID()));
		_logger.info("Starting container services.");
		ContainerServices.loadAll();
		ContainerServices.startAll();
		
		initializeServices(webAppCtxt);
		
		ServiceDeployer.startServiceDeployer(_axisServer,
			Installation.getDeployment(
				new DeploymentName()).getServicesDirectory());
	}
	
	static private void initializeServices(WebAppContext ctxt)
		throws ServletException, AxisFault
	{
		ServletHolder []holders = ctxt.getServletHandler().getServlets();
		for (ServletHolder holder : holders)
		{
			if (holder.getName().equals("AxisServlet"))
			{
				_axisServer = ((AxisServletBase) holder.getServlet()).getEngine();
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
		Security resourceIdSecProps = 
			Installation.getDeployment(new DeploymentName()).security();
		
		String keyStoreLoc = resourceIdSecProps.getProperty(
			SecurityConstants.Container.RESOURCE_IDENTITY_KEY_STORE_PROP);
		String keyStoreType = resourceIdSecProps.getProperty(
			SecurityConstants.Container.RESOURCE_IDENTITY_KEY_STORE_TYPE_PROP,
			"PKCS12");
		String keyPassword = resourceIdSecProps.getProperty(
			SecurityConstants.Container.RESOURCE_IDENTITY_KEY_PASSWORD_PROP);
		String keyStorePassword = resourceIdSecProps.getProperty(
			SecurityConstants.Container.RESOURCE_IDENTITY_KEY_STORE_PASSWORD_PROP);
		String containerAlias = resourceIdSecProps.getProperty(
			SecurityConstants.Container.RESOURCE_IDENTITY_CONTAINER_ALIAS_PROP, 
			GenesisIIConstants.CONTAINER_CERT_ALIAS);
		
		String certificateLifetime = resourceIdSecProps.getProperty(
			SecurityConstants.Container.RESOURCE_IDENTITY_DEFAULT_CERT_LIFETIME_PROP);
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

		KeyStore ks = CertTool.openStoreDirectPath(
			Installation.getDeployment(
				new DeploymentName()).security().getSecurityFile(keyStoreLoc),
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
	
	static public JavaServiceDesc findService(EndpointReferenceType epr)
		throws AxisFault
	{
		return findService(epr.getAddress());
	}
	
	static public JavaServiceDesc findService(AttributedURIType uri)
		throws AxisFault
	{
		return findService(uri.get_value());
	}
	
	static public JavaServiceDesc findService(URI uri)
		throws AxisFault
	{
		return findService(uri.getPath());
	}
	
	static public JavaServiceDesc findService(java.net.URI uri)
		throws AxisFault
	{
		return findService(uri.getPath());
	}
	
	static public JavaServiceDesc findService(String pathOrName)
		throws AxisFault
	{
		int index = pathOrName.lastIndexOf('/');
		if (index >= 0)
			pathOrName = pathOrName.substring(index + 1);
		
		SOAPService ss = _axisServer.getService(pathOrName);
		return (JavaServiceDesc)ss.getServiceDescription(); 
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
				return currentURL.substring(0, index + 1) + serviceName +
					"?" + EPRUtils.GENII_CONTAINER_ID_PARAMETER + "=" 
					+ Container.getContainerID();
		}
		
		return _containerURL + "/axis/services/" + serviceName +
			"?" + EPRUtils.GENII_CONTAINER_ID_PARAMETER + "=" + Container.getContainerID();
	}
	
	static public boolean onThisServer(EndpointReferenceType target)
	{
		String urlString = target.getAddress().toString();
		String containerURL = _containerURL + "/axis/services/";
		
		if (urlString.startsWith(containerURL))
			return true;
		
		return false;
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
	
	static private GUID _containerID = null;
	static private Object _containerIDLock = new Object();
	
	static public GUID getContainerID()
	{
		synchronized (_containerIDLock)
		{
			if (_containerID == null)
			{
				PersistentContainerProperties properties =
					PersistentContainerProperties.getProperties();
				try
				{
					_containerID = (GUID)properties.getProperty("container-id");
					if (_containerID == null)
						_containerID = new GUID();
					properties.setProperty("container-id", _containerID);
				}
				catch (Throwable cause)
				{
					throw new ConfigurationException(
						"Unable to get/set container id.", cause);
				}
			}
			
			return _containerID;
		}
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
