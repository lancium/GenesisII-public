package edu.virginia.vcgr.genii.client.exec.install.windows;

import java.io.File;
import java.io.IOException;

import edu.virginia.vcgr.genii.client.exec.AbstractExecutionTask;
import edu.virginia.vcgr.genii.client.exec.ExecutionTask;

public class WindowsRights
{
	static private class GrantRightsTask extends AbstractExecutionTask
	{
		private File _rightsExe;
		private String _rightToken;
		private String _account;
		
		private GrantRightsTask(File rightsExe, String rightToken, 
			String account) throws IOException
		{
			CommonFunctions.checkPath(
				rightsExe, true);
			
			_rightsExe = rightsExe;
			_rightToken = rightToken;
			_account = account;
		}
		
		@Override
		public String[] getCommandLine()
		{
			return new String[] {
				_rightsExe.getAbsolutePath(),
				"+r", _rightToken, "-u", _account
			};
		}
		
		@Override
		public String toString()
		{
			return String.format("Grant right %s to %s",
				_rightToken, _account);
		}
	}
	
	static public ExecutionTask grantLogonAsService(String account)
		throws IOException
	{
		account = CommonFunctions.getAccount(account);
		File geniiInstallDir = CommonFunctions.getGeniiInstallDir();
		File rightsExe = new File(geniiInstallDir, 
			"ext\\WindowsResourceKits\\Tools\\ntrights.exe");
		CommonFunctions.checkPath(rightsExe, true);
		
		return new GrantRightsTask(rightsExe, "SeServiceLogonRight", account);
	}
}