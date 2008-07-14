package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.File;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.fuse.server.GeniiFuse;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.utils.SystemExec;

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
	
	public FuseTool()
	{
		super(_DESCRIPTION, _USAGE, false);
	}
	
	public void setMount()
	{
		_isMount = true;
	}
	
	public void setUnmount()
	{
		_isUnmount = true;
	}
	
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
				String str = SystemExec.executeForOutput("id", "-u");
				_uid = Integer.parseInt(str);
			}
			catch (Throwable cause)
			{
				_logger.warn("Unable to automatically determine uid.", cause);
				_uid = 0;
			}
		}
		
		if (_isMount)
		{
			GeniiFuse.mountGenesisII(new File(getArgument(0)), 
				new String[] { "-f", "-s" }, _uid);
		} else
		{
			GeniiFuse.unmountGenesisII(new File(getArgument(0)));
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
