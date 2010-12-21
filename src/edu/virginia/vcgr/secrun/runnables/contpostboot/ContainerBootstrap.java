package edu.virginia.vcgr.secrun.runnables.contpostboot;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.client.cmd.GetHostName;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.cmd.tools.AttachHostTool;
import edu.virginia.vcgr.genii.client.cmd.tools.BaseLoginTool;
import edu.virginia.vcgr.genii.client.cmd.tools.KeystoreLoginTool;
import edu.virginia.vcgr.genii.client.cmd.tools.LoginTool;
import edu.virginia.vcgr.genii.client.cmd.tools.LogoutTool;
import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;
import edu.virginia.vcgr.genii.client.configuration.DeploymentName;
import edu.virginia.vcgr.genii.client.configuration.Installation;
import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ContextStreamUtils;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.context.MemoryBasedContextResolver;
import edu.virginia.vcgr.genii.client.resource.ResourceException;
import edu.virginia.vcgr.genii.container.configuration.ContainerConfiguration;
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
		OwnerInfo info = new OwnerInfo();
		BootstrapProperties bProperties = new BootstrapProperties();
		ContainerConfiguration cc = new ContainerConfiguration(
			ConfigurationManager.getCurrentConfiguration());
		
		try
		{
			out = new OutputStreamWriter(System.out);
			err = new OutputStreamWriter(System.err);
			in = new InputStreamReader(System.in);
			
			String hostname = getHostName(false, true);
			String connectURL = bProperties.getConnectURL();
			
			ICallingContext callingContext = connect(connectURL);
			try
			{
				ContextManager.setResolver(new MemoryBasedContextResolver(
					callingContext));
				login(bProperties, info);
				
				AttachHostTool tool = new AttachHostTool();
				tool.addArgument(String.format(
					"http%s://localhost:%d/axis/services/VCGRContainerPortType",
					cc.isSSL() ? "s" : "", cc.getListenPort()));
				tool.addArgument(String.format("%s/%s",
					bProperties.getAttachPath(), hostname));
				if (tool.run(out, err, in) != 0)
					throw new ToolException("Unable to attach host to net.");
				info.deleteFile();
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
	
	private void login(
		BootstrapProperties cProperties, OwnerInfo ownerInfo)
			throws Throwable
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
			
			BaseLoginTool tool = new KeystoreLoginTool();
			
			//Assume certificate based
			tool.setNo_gui();
			tool.addArgument("local:" + Installation.getDeployment(
				new DeploymentName()).security().getSecurityFile(
					certStore).getAbsolutePath());
			tool.setStoretype(certStoreType);
			tool.setPattern(certPattern);
			if (certPassword != null)
				tool.setPassword(certPassword);
			if (tool.run(out, err, in) != 0)
				throw new ToolException("Unable to log in as installer.");
			
			//Assume certificate based
			tool = new KeystoreLoginTool();
			tool.setNo_gui();
			tool.addArgument("local:" + Installation.getDeployment(
				new DeploymentName()).security().getSecurityFile(
					cProperties.getCertGeneratorOutputStoreName()).getAbsolutePath());
			tool.setStoretype("PKCS12");
			tool.setAlias();
			tool.setPattern(cProperties.getCertGeneratorAlias());
			tool.setPassword(cProperties.getCertGeneratorPassword());
			if (tool.run(out, err, in) != 0)
				throw new ToolException("Unable to log in as container.");
			
			//Assume IDP?
			tool = new LoginTool();
			tool.setNo_gui();
			tool.addArgument(String.format(
				"rns:%s", ownerInfo.getUserPath()));
			tool.setUsername(ownerInfo.getUserName());
			tool.setPassword(ownerInfo.getUserPassword());
			if (tool.run(out, err, in) != 0)
				throw new ToolException("Unable to log in as owner.");
		}
		finally
		{
			mgr.setRoleServer();
		}
	}	
}