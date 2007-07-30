package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;

import org.morgan.util.configuration.ConfigurationException;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;

public class RmTool extends BaseGridTool
{
	static private final String _DESCRIPTION =
		"Removes the target paths.";
	static private final String _USAGE =
		"rm [-rf | --recursive --force] <target-path> ...";
	
	private boolean _recursive = false;
	private boolean _force = false;
	
	public void setRecursive()
	{
		_recursive = true;
	}
	
	public void setR()
	{
		setRecursive();
	}
	
	public void setForce()
	{
		_force = true;
	}
	
	public void setF()
	{
		setForce();
	}
	
	public RmTool()
	{
		super(_DESCRIPTION, _USAGE, false);
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		boolean recursive = _recursive;
		boolean force = _force;
		
		RNSPath path = RNSPath.getCurrent();
			
		for (int lcv = 0; lcv < numArguments(); lcv++)
		{
			rm(path, getArgument(lcv), recursive, force);
		}
			
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() < 1)
			throw new InvalidToolUsageException();
	}
	
	public void rm(RNSPath currentPath,
		String filePath, boolean recursive, boolean force)
		throws RNSException, ConfigurationException, IOException
	{
		RNSPath [] files = currentPath.list(
			filePath, RNSPathQueryFlags.MUST_EXIST);
		
		for (RNSPath file : files)
		{
			rm(file, recursive, force);
		}
	}
	
	public void rm(RNSPath path, boolean recursive, 
		boolean force) throws RNSException
	{
		try
		{
			if (recursive)
				path.recursiveDelete();
			else
				path.delete();
		}
		catch (RNSException re)
		{
			if (force)
			{
				stderr.println("Forcing removal after exception");
				
				path.unlink();
			} else
				throw re;
		}
	}
}