package edu.virginia.vcgr.secrun.runnables.contpreboot;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.cmd.GetHostName;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.cmd.tools.CertGeneratorTool;
import edu.virginia.vcgr.genii.client.cmd.tools.GamlLoginTool;
import edu.virginia.vcgr.genii.client.cmd.tools.LogoutTool;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.configuration.DeploymentName;
import edu.virginia.vcgr.genii.client.configuration.Installation;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ContextStreamUtils;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.context.MemoryBasedContextResolver;
import edu.virginia.vcgr.genii.client.postlog.ExceptionEvent;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.secrun.SecureRunnable;

public class ContainerBootstrap implements SecureRunnable
{
	static private Log _logger = LogFactory.getLog(ContainerBootstrap.class);
	
	private Writer out = null;
	private Writer err = null;
	private Reader in = null;
	
	@Override
	public boolean run(Properties runProperties) throws Throwable
	{
		BootstrapProperties bProperties = new BootstrapProperties();

		try
		{
			out = new OutputStreamWriter(System.out);
			err = new OutputStreamWriter(System.err);
			in = new InputStreamReader(System.in);
			
			String connectURL = bProperties.getConnectURL();
			if (connectURL == null)
				throw new ConfigurationException(
					"The container.properties file did not contain " +
					"a connect url.");
			
			String hostname = getHostName(false, true);
			
			ICallingContext callingContext = ContextManager.getCurrentContext(false);
			if (callingContext == null)
				callingContext = connect(connectURL);
			
			try
			{
				ContextManager.setResolver(new MemoryBasedContextResolver(
					callingContext));
				loginAsInstaller(bProperties);
				generateContainerCertificate(bProperties, hostname);
				generateContainerPublicCertificate(bProperties);
			}
			finally
			{
				ContextManager.setResolver(null);
			}
			
			return true;
		}
		catch (Throwable cause)
		{
			_logger.error("Unable to bootstrap container.", cause);
			bProperties.getExceptionLogger().post(new ExceptionEvent(
				"Unable to bootstrap container.", cause));
			throw cause;
		}
	}
	
	static private String getHostName(boolean getIP, boolean fullyQualified)
		throws ConfigurationException
	{
		try
		{
			if (getIP)
				return GetHostName.getHostNameIP();
			else
			{
				String hostname = GetHostName.getHostName();
				if (!fullyQualified)
				{
					int index = hostname.indexOf('.');
					if (index > 0)
						hostname = hostname.substring(0, index);
				}

				return hostname;
			}
		}
		catch (SocketException se)
		{
			throw new ConfigurationException("Unable to get host name.", se);
		}
	}
	
	static private ICallingContext connect(String connectURL)
		throws ResourceException, MalformedURLException, IOException
	{
		return ContextStreamUtils.load(new URL(connectURL));
	}
	
	private void loginAsInstaller(
		BootstrapProperties cProperties) throws Throwable
	{
		String certStore = cProperties.getInstallerCertStorePath();
		if (certStore == null)
			throw new ConfigurationException(
				"Unable to find installer certificate store path property.");
		
		String certStoreType = cProperties.getInstallerCertStoreType();
		if (certStoreType == null)
			certStoreType = "PKCS12";
		
		String certPattern = cProperties.getInstallerCertPattern();
		if (certPattern == null)
			throw new ConfigurationException(
				"Unable to find installer certificate pattern property.");
		
		String certPassword = cProperties.getInstallerCertStorePassword();
		
		ConfigurationManager mgr =
			ConfigurationManager.getCurrentConfiguration();
		try
		{
			mgr.setRoleClient();
			LogoutTool outTool = new LogoutTool();
			outTool.setAll();
			outTool.setNo_gui();
			outTool.run(out, err, in);
			
			GamlLoginTool tool = new GamlLoginTool();
			tool.setNo_gui();
			tool.addArgument(Installation.getDeployment(
				new DeploymentName()).security().getSecurityFile(
					certStore).getAbsolutePath());
			tool.setStoretype(certStoreType);
			tool.setPattern(certPattern);
			if (certPassword != null)
				tool.setPassword(certPassword);
			if (tool.run(out, err, in) != 0)
				throw new ToolException("Unable to log in as installer.");
		}
		finally
		{
			mgr.setRoleServer();
		}
	}
	
	private void generateContainerCertificate(BootstrapProperties bProperties,
		String hostname) throws Throwable
	{
		CertGeneratorTool tool = new CertGeneratorTool();
		tool.setGen_cert();
		tool.addArgument(bProperties.getCertGeneratorRNSPath());
		tool.setKeysize(bProperties.getCertGeneratorKeysize());
		tool.setKs_path(
			Installation.getDeployment(
				new DeploymentName()).security().getSecurityFile(
					bProperties.getCertGeneratorOutputStoreName()).getAbsolutePath());
		tool.setKs_pword(bProperties.getCertGeneratorPassword());
		tool.setKs_alias(bProperties.getCertGeneratorAlias());
		tool.setCn(hostname);
		tool.setOu(bProperties.getCertGeneratorOU());
		tool.setO(bProperties.getCertGeneratorO());
		tool.setL(bProperties.getCertGeneratorL());
		tool.setC(bProperties.getCertGeneratorC());
		tool.setSt(bProperties.getCertGeneratorST());
		if (tool.run(out, err, in) != 0)
			throw new ToolException("Unable to generate container certificate.");
	}
	 
	static private void generateContainerPublicCertificate(
		BootstrapProperties bProperties) throws Throwable
	{
		KeyStore kStore = KeyStore.getInstance("PKCS12", "BC");

		InputStream stream = null;
		OutputStream out = null;
		try
		{
			stream = new FileInputStream(
				Installation.getDeployment(new DeploymentName()).security().getSecurityFile(
				bProperties.getCertGeneratorOutputStoreName()));
			kStore.load(stream, 
				bProperties.getCertGeneratorPassword().toCharArray());
			Certificate cert = kStore.getCertificate(bProperties.getCertGeneratorAlias());
			X509Certificate xCert = (X509Certificate)cert;
			
			out = new FileOutputStream(Installation.getDeployment(
				new DeploymentName()).security().getSecurityFile(
					bProperties.getContainerPublicCertFilename()));
			out.write(xCert.getEncoded());
		}
		finally
		{
			StreamUtils.close(out);
			 StreamUtils.close(stream);
		}
	 }
}