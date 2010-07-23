package edu.virginia.vcgr.genii.client.cmd.tools;

import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.configuration.DeploymentName;
import edu.virginia.vcgr.genii.client.configuration.Installation;
import edu.virginia.vcgr.genii.client.configuration.WebContainerConstants;
import edu.virginia.vcgr.genii.client.exec.ExecutionEngine;
import edu.virginia.vcgr.genii.client.exec.ExecutionTask;
import edu.virginia.vcgr.genii.client.exec.install.windows.GenesisIIContainerService;
import edu.virginia.vcgr.genii.client.exec.install.windows.WindowsFirewall;
import edu.virginia.vcgr.genii.client.exec.install.windows.WindowsRights;
import edu.virginia.vcgr.genii.client.exec.install.windows.WindowsServices;

public class ManageWindowsContainerServiceTool extends BaseGridTool
{
	static private Log _logger = LogFactory.getLog(
		ManageWindowsContainerServiceTool.class);
	
	static final private String USAGE =
		"manage-windows-container-service { -u | -i } [--service-name=<service-name>]\n" +
		"\t[--account=<account>] [--password=<password>]\n\n" +
		"\t\tIf the <account> string has the token $MACHINENAME in it,\n" +
		"\t\tthat token will be replaced with the short name of the\n" +
		"\t\tlocal machine.";
	static final private String DESCRIPTION =
		"Installs or uninstalls a container as a windows service.";
	
	private boolean _install = false;
	private boolean _uninstall = false;
	private String _serviceName = "Genesis II Container";
	private String _account = null;
	private String _password = null;
	
	public ManageWindowsContainerServiceTool()
	{
		super(DESCRIPTION, USAGE, true);
	}
	
	@Option({"service-name"})
	public void setService_name(String serviceName)
	{
		_serviceName = serviceName;
	}
	
	@Option({"account"})
	public void setAccount(String account)
	{
		_account = account;
	}
	
	@Option({"password"})
	public void setPassword(String password)
	{
		_password = password;
	}
	
	@Option({"i"})
	public void setI()
	{
		_install = true;
	}
	
	@Option({"u"})
	public void setU()
	{
		_uninstall = true;
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		int result = 0;
		
		Properties webContainerProperties = Installation.getDeployment(
			new DeploymentName()).webContainerProperties();
		int port = Integer.parseInt(webContainerProperties.getProperty(
			WebContainerConstants.LISTEN_PORT_PROP, "18080"));
		
		ExecutionTask tasks[];
		
		if (_install)
			tasks = new ExecutionTask[] {
				GenesisIIContainerService.installGenesisIIContainer(
					_account, _password),
				WindowsRights.grantLogonAsService(_account),
				WindowsFirewall.createOpenPortTask(_serviceName, port, 
					WindowsFirewall.FirewallPortTypes.TCP),
				WindowsServices.createStartServiceTask("Genesis II Container"),
			};
		else
			tasks = new ExecutionTask[] {
				WindowsServices.createStopServiceTask("Genesis II Container"),
				WindowsFirewall.createClosePortTask(port,
					WindowsFirewall.FirewallPortTypes.TCP),
				GenesisIIContainerService.uninstallGenesisIIContainer()
			};
		
		for (ExecutionTask eTask : tasks)
		{
			boolean success = false;
			for (int lcv = 0; lcv < 5; lcv++)
			{
				try
				{
					ExecutionEngine.execute(null, System.out, System.err,
						eTask);
					success = true;
					break;
				}
				catch (Throwable cause)
				{
					_logger.error(String.format(
						"Unable to execute task(%s).", eTask), cause);
					try { Thread.sleep(1000L * 5); } catch (InterruptedException ie) {}
				}
			}
			
			if (!success)
			{
				if (_install)
				{
					_logger.error(String.format(
						"Failed to execute task(%s) with all retries -- " +
						"giving up.", eTask));
					return 1;
				} else
				{
					_logger.error(String.format(
						"Failed to execute task(%s) with all retries -- " +
						"we'll keep going just in case.", eTask));
					result = 1;
				}
			}
		}
		
		return result;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (_uninstall == _install)
			throw new InvalidToolUsageException(
				"Must specify either install or uninstall flag.");
	}
}