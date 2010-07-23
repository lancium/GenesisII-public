package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.fuse.GeniiFuse;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.utils.exec.ExecutionEngine;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.gpath.GeniiPathType;

public class FuseTool extends BaseGridTool
{
	static private Log _logger = LogFactory.getLog(FuseTool.class);
	
	static final private String _DESCRIPTION =
		"Mounts and unmounts a FUSE file system.";
	static final private FileResource _USAGE =
		new FileResource("edu/virginia/vcgr/genii/client/cmd/tools/resources/fuse-usage.txt");
	
	private boolean _isMount = false;
	private boolean _isUnmount = false;
	private int _uid = -1;
	private boolean _daemon = false;
	private String _sandbox = null;
	
	public FuseTool()
	{
		super(_DESCRIPTION, _USAGE, false);
	}
	
	@Option({"daemon"})
	public void setDaemon()
	{
		_daemon = true;
	}
	
	@Option({"sandbox"})
	public void setSandbox(String sandbox)
	{
		_sandbox = sandbox;
	}

	@Option({"mount"})
	public void setMount()
	{
		_isMount = true;
	}
	
	@Option({"unmount"})
	public void setUnmount()
	{
		_isUnmount = true;
	}
	
	@Option({"uid"})
	public void setUid(String uid)
	{
		_uid = Integer.parseInt(uid);
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		if (_uid < 0)
		{
			try
			{
				String str = ExecutionEngine.simpleExecute("id", "-u");
				_uid = Integer.parseInt(str);
			}
			catch (Throwable cause)
			{
				_logger.warn("Unable to automatically determine uid.", cause);
				_uid = 0;
			}
		}
		
		GeniiPath gPath = new GeniiPath(getArgument(0));
		if ( gPath.pathType() != GeniiPathType.Local)
			throw new InvalidToolUsageException("mount-point must be a local path beginning with 'local:' ");
		if (_isMount)
		{
			GeniiFuse.mountGenesisII(new File(gPath.path()), 
				new String[] { "-f", "-s" }, null, _sandbox, _uid, _daemon);
		} else
		{
			GeniiFuse.unmountGenesisII(new File(gPath.path()));
		}
		
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		int count = _isMount ? 1 : 0;
		count += _isUnmount ? 1 : 0;
		
		if (count != 1)
			throw new InvalidToolUsageException(
				"Couldn't determine if user intended to mount, or unmount.");
		
		if (numArguments() != 1)
			throw new InvalidToolUsageException(
				"Mount point not correctly specified.");
	}
}
