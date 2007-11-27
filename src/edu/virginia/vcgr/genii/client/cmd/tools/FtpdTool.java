package edu.virginia.vcgr.genii.client.cmd.tools;

import org.morgan.ftp.FTPConfiguration;
import org.morgan.ftp.FTPDaemon;
import org.morgan.ftp.NetworkConstraint;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.ftp.GeniiBackendConfiguration;
import edu.virginia.vcgr.genii.ftp.GeniiBackendFactory;

public class FtpdTool extends BaseGridTool
{
	static private final String _DESCRIPTION =
		"Runs an FTP daemon on the given port.";
	static private final String _USAGE_RESOURCE =
		"edu/virginia/vcgr/genii/client/cmd/tools/resources/ftpd-usage.txt";
	
	private int _idleTimeout = -1;
	private int _dataConnectionTimeout = -1;
	private int _maxAuthAttempts = -1;
	private String _sandbox = null;
	
	public FtpdTool()
	{
		super(_DESCRIPTION, new FileResource(_USAGE_RESOURCE), false);
	}
	
	public void setIdle_timeout(String seconds)
	{
		_idleTimeout = Integer.parseInt(seconds);
	}
	
	public void setData_connection_timeout(String timeout)
	{
		_dataConnectionTimeout = Integer.parseInt(timeout);
	}
	
	public void setMax_auth_attempts(String max)
	{
		_maxAuthAttempts = Integer.parseInt(max);
	}
	
	public void setSandbox(String sandBox)
	{
		_sandbox = sandBox;
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		GeniiBackendConfiguration backConf = new GeniiBackendConfiguration();
		
		FTPConfiguration conf = new FTPConfiguration(
			Integer.parseInt(getArgument(0)));
		if (_idleTimeout >= 0)
			conf.setIdleTimeoutSeconds(_idleTimeout);
		if (_dataConnectionTimeout >= 0)
			conf.setDataConnectionTimeoutSeconds(_dataConnectionTimeout);
		if (_maxAuthAttempts >= 0)
			conf.setMissedAuthenticationsLimit(_maxAuthAttempts);
		
		if (_sandbox != null)
			backConf.setSandboxPath(_sandbox);
		
		if (numArguments() > 1)
		{
			NetworkConstraint []constraints =
				new NetworkConstraint[numArguments() - 1];
			
			for (int lcv = 1; lcv < numArguments(); lcv++)
				constraints[lcv - 1] = new NetworkConstraint(
					getArgument(lcv));
			
			conf.setNetworkConstraints(constraints);
		}
		
		FTPDaemon daemon = new FTPDaemon(new GeniiBackendFactory(backConf), conf);
		daemon.start();
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() < 1)
			throw new InvalidToolUsageException();
	}
}