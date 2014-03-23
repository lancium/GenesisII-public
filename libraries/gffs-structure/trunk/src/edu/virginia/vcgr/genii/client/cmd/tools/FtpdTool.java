package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;

import org.morgan.ftp.FTPConfiguration;
import org.morgan.ftp.FTPDaemon;
import org.morgan.ftp.FTPException;
import org.morgan.ftp.NetworkConstraint;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ReloadShellException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.dialog.DialogException;
import edu.virginia.vcgr.genii.client.dialog.UserCancelException;
import edu.virginia.vcgr.genii.client.io.LoadFileResource;
import edu.virginia.vcgr.genii.client.rcreate.CreationException;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rp.ResourcePropertyException;
import edu.virginia.vcgr.genii.client.security.axis.AuthZSecurityException;
import edu.virginia.vcgr.genii.network.ftp.GeniiBackendConfiguration;
import edu.virginia.vcgr.genii.network.ftp.GeniiBackendFactory;

public class FtpdTool extends BaseGridTool
{
	static private final String _DESCRIPTION = "config/tooldocs/description/dftpd";
	static private final String _USAGE_RESOURCE = "config/tooldocs/usage/uftpd";
	static private final String _MANPAGE = "config/tooldocs/man/ftpd";

	private boolean _block = false;
	private int _idleTimeout = -1;
	private int _dataConnectionTimeout = -1;
	private int _maxAuthAttempts = -1;
	private String _sandbox = null;

	static private FTPDaemon _daemon = null;

	public FtpdTool()
	{
		super(new LoadFileResource(_DESCRIPTION), new LoadFileResource(_USAGE_RESOURCE), true, ToolCategory.ANTIQUATED);
		addManPage(new LoadFileResource(_MANPAGE));
	}

	@Option({ "block" })
	public void setBlock()
	{
		_block = true;
	}

	@Option({ "idle-timeout" })
	public void setIdle_timeout(String seconds)
	{
		_idleTimeout = Integer.parseInt(seconds);
	}

	@Option({ "data-connection-timeout" })
	public void setData_connection_timeout(String timeout)
	{
		_dataConnectionTimeout = Integer.parseInt(timeout);
	}

	@Option({ "max-auth-attempts" })
	public void setMax_auth_attempts(String max)
	{
		_maxAuthAttempts = Integer.parseInt(max);
	}

	@Option({ "sandbox" })
	public void setSandbox(String sandBox)
	{
		_sandbox = sandBox;
	}

	@Override
	protected int runCommand() throws ReloadShellException, ToolException, UserCancelException, RNSException,
		AuthZSecurityException, IOException, ResourcePropertyException, CreationException, InvalidToolUsageException,
		ClassNotFoundException, DialogException
	{
		String arg = getArgument(0);

		if (arg.equals("stop")) {
			synchronized (FtpdTool.class) {
				if (_daemon == null) {
					stderr.println("There is no ftpd currently running.");
					return 1;
				}

				try {
					_daemon.stop();
				} catch (FTPException e) {
					throw new ToolException("failure to stop ftp daemon: " + e.getLocalizedMessage(), e);
				}
				_daemon = null;
			}

			return 0;
		}

		GeniiBackendConfiguration backConf = new GeniiBackendConfiguration(stdin, stdout, stderr);

		FTPConfiguration conf = new FTPConfiguration(Integer.parseInt(arg));

		synchronized (FtpdTool.class) {
			if (_daemon != null) {
				this.stderr.println("An ftpd is already running on port " + _daemon.getPort());
				return 1;
			}

			if (_idleTimeout >= 0)
				conf.setIdleTimeoutSeconds(_idleTimeout);
			if (_dataConnectionTimeout >= 0)
				conf.setDataConnectionTimeoutSeconds(_dataConnectionTimeout);
			if (_maxAuthAttempts >= 0)
				conf.setMissedAuthenticationsLimit(_maxAuthAttempts);

			if (_sandbox != null)
				backConf.setSandboxPath(_sandbox);
			else
				backConf.setSandboxPath("/");

			if (numArguments() > 1) {
				NetworkConstraint[] constraints = new NetworkConstraint[numArguments() - 1];

				for (int lcv = 1; lcv < numArguments(); lcv++)
					constraints[lcv - 1] = new NetworkConstraint(getArgument(lcv));

				conf.setNetworkConstraints(constraints);
			}

			_daemon = new FTPDaemon(new GeniiBackendFactory(backConf), conf);
			try {
				_daemon.start();
			} catch (FTPException e) {
				throw new ToolException("failure to start ftp daemon: " + e.getLocalizedMessage(), e);
			}
		}

		stdout.format("FTP Daemon started on port %d\n", conf.getListenPort());

		while (_block) {
			try {
				Thread.sleep(1000 * 60 * 60 * 24 * 7);
			} catch (Throwable cause) {
			}
		}

		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() < 1)
			throw new InvalidToolUsageException();
	}
}
