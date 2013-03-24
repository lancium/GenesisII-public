package edu.virginia.vcgr.genii.client.exec.install.windows;

import java.io.File;
import java.io.IOException;

import edu.virginia.vcgr.genii.client.exec.ExecutionTask;
import edu.virginia.vcgr.genii.client.exec.install.windows.JavaServiceWrapper;

public class GenesisIIContainerService
{
	static public ExecutionTask installGenesisIIContainer(String account, String accountPassword) throws IOException
	{
		account = CommonFunctions.getAccount(account);

		File geniiInstallDir = CommonFunctions.getGeniiInstallDir();
		File wrapperExe = new File(geniiInstallDir, "ext\\JavaServiceWrapper\\bin\\wrapper.exe");
		File configurationFile = new File(geniiInstallDir, "ext\\JavaServiceWrapper\\conf\\runContainer.conf");
		File localJavaDir = new File(geniiInstallDir, "Java\\windows-i586\\jre");

		JavaServiceWrapper.JavaServiceWrapperTask task = JavaServiceWrapper.installServiceWrapper(wrapperExe,
			configurationFile, account, accountPassword);

		task.addVariable("LOCAL_JAVA_DIR", localJavaDir.getAbsolutePath());
		task.addVariable("USER_NAME_STRING", account);
		task.addVariable("GENII_INSTALL_DIR", geniiInstallDir.getAbsolutePath());

		return task;
	}

	static public ExecutionTask uninstallGenesisIIContainer() throws IOException
	{
		File geniiInstallDir = CommonFunctions.getGeniiInstallDir();
		File wrapperExe = new File(geniiInstallDir, "ext\\JavaServiceWrapper\\bin\\wrapper.exe");
		File configurationFile = new File(geniiInstallDir, "ext\\JavaServiceWrapper\\conf\\runContainer.conf");
		File localJavaDir = new File(geniiInstallDir, "Java\\windows-i586\\jre");

		JavaServiceWrapper.JavaServiceWrapperTask task = JavaServiceWrapper.uninstallServiceWrapper(wrapperExe,
			configurationFile);

		task.addVariable("LOCAL_JAVA_DIR", localJavaDir.getAbsolutePath());
		task.addVariable("GENII_INSTALL_DIR", geniiInstallDir.getAbsolutePath());

		return task;
	}
}