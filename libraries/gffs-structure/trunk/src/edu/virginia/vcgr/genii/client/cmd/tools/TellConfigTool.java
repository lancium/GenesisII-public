package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.File;
import java.io.IOException;

import edu.virginia.vcgr.genii.client.InstallationProperties;
import edu.virginia.vcgr.genii.client.cmd.ReloadShellException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.configuration.DeploymentName;
import edu.virginia.vcgr.genii.client.configuration.Installation;
import edu.virginia.vcgr.genii.client.configuration.SslInformation;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.security.VerbosityLevel;

public class TellConfigTool extends BaseGridTool
{
	static final private String _DESCRIPTION = "config/tooldocs/description/dtell-config";
	static final private String _USAGE = "config/tooldocs/usage/utell-config";
	static final private String _MANPAGE = "config/tooldocs/man/tell-config";

	static final public String ARG_DEPLOYMENT_NAME = "deployment-name";
	static final public String ARG_DEPLOYMENTS_DIR = "deployments-dir";
	static final public String ARG_ACTIVE_DEPLOYMENTS_DIR = "active-deployment-dir";
	static final public String ARG_SECURITY_DIR = "security-dir";
	static final public String ARG_STATE_DIR = "state-dir";
	static final public String ARG_TLS_KEYPAIR = "tls-keypair";
	static final public String ARG_OWNER_CERT = "owner-cert";
	static final public String ARG_ADMIN_CERT = "admin-cert";

	public TellConfigTool()
	{
		super(new LoadFileResource(_DESCRIPTION), new LoadFileResource(_USAGE), false);
		addManPage(new LoadFileResource(_MANPAGE));
	}

	@Override
	protected int runCommand() throws ReloadShellException, ToolException, UserCancelException, RNSException,
		AuthZSecurityException, IOException, ResourcePropertyException
	{
		int toReturn = 0;

		if (numArguments() == 0) {
			stdout.println("No configuration item requested; try 'grid man tell-config'");
		}

		for (int i = 0; i < numArguments(); i++) {
			String arg = getArgument(i);

			switch (arg) {
				case ARG_DEPLOYMENT_NAME: {
					stdout.println("Deployment name is '" + Installation.getDeployment(new DeploymentName()).getName() + "'");
					break;
				}
				case ARG_DEPLOYMENTS_DIR: {
					stdout.println("Deployments top-level directory is '" + Installation.getDeploymentsDirectory() + "'");
					break;
				}
				case ARG_ACTIVE_DEPLOYMENTS_DIR: {
					stdout.println("Active Deployment directory is '"
						+ Installation.getDeployment(new DeploymentName()).getDeploymentTop().lookupFile("").getAbsolutePath()
						+ "'");
					break;
				}
				case ARG_SECURITY_DIR: {
					File firstLevel =
						InstallationProperties.getInstallationProperties().getLocalCertsDirectory().lookupFile("");
					File secondLevel =
						Installation.getDeployment(new DeploymentName()).security().getSecurityDirectory().lookupFile("");
					String report = "Security directory is '" + firstLevel.getAbsolutePath() + "'";
					if (!firstLevel.getName().equals(secondLevel.getName())) {
						report = report + "\nFallback Security directory is: '" + secondLevel.getAbsolutePath() + "'";
					}
					stdout.println(report);
					break;
				}
				case ARG_STATE_DIR: {
					stdout.println("State directory is '" + InstallationProperties.getUserDir() + "'");
					break;
				}
				case ARG_TLS_KEYPAIR: {
					String report = "";
					SslInformation si = new SslInformation(Installation.getDeployment(new DeploymentName()).security());
					String tlsCertFile = si.getKeystoreFilename();
					if (tlsCertFile != null) {
						report = report + "Container TLS Keypair stored in: '" + tlsCertFile + "'.";
					} else {
						report = report + "Did not find Container TLS Keypair";
					}
					stdout.println(report);
					break;
				}
				case ARG_OWNER_CERT: {
					File certFile = InstallationProperties.getInstallationProperties().getOwnerCertFile();
					stdout.println("Owner Certificate stored in: '" + certFile + "'");
					if (certFile != null) {
						stdout.println("Owner Certificate Subject: '"
							+ InstallationProperties.getInstallationProperties().getOwnerCertificate()
								.describe(VerbosityLevel.LOW) + "'");
					}
					break;
				}
				case ARG_ADMIN_CERT: {
					File certFile = Installation.getDeployment(new DeploymentName()).security().getAdminCertFile();
					stdout.println("Admin Certificate stored in: '" + certFile + "'");
					if (certFile != null) {
						stdout.println("Admin Certificate Subject: '"
							+ Installation.getDeployment(new DeploymentName()).security().getAdminIdentity()
								.describe(VerbosityLevel.LOW) + "'");
					}
					break;
				}
				// makes no sense since tools are always in client role.
				// case ARG_ROLE: {
				// String role;
				// if (ConfigurationManager.getCurrentConfiguration().isServerRole()) {
				// role = "Server";
				// } else {
				// role = "Client";
				// }
				// stdout.println("Installation is in a " + role + " role.");
				// break;
				// }
				default: {
					stdout.println("Unknown argument for tell-config: '" + arg + "'");
					toReturn = 1;
					break;
				}
			}
			stdout.flush();
		}
		return toReturn;
	}

	@Override
	protected void verify() throws ToolException
	{
	}

	@Override
	public void addArgument(String argument)
	{
		_arguments.add(argument);
	}
}
