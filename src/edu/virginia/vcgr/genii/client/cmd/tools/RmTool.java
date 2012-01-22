package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.File;
import java.io.IOException;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSUtilities;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.gpath.GeniiPathType;
import edu.virginia.vcgr.genii.client.io.FileResource;
public class RmTool extends BaseGridTool
{
	static private final String _DESCRIPTION =
		"edu/virginia/vcgr/genii/client/cmd/tools/description/drm";
	static private final String _USAGE =
		"edu/virginia/vcgr/genii/client/cmd/tools/usage/urm";
	static private final String _MANPAGE =
		"edu/virginia/vcgr/genii/client/cmd/tools/man/rm";
	
	private boolean _recursive = false;
	private boolean _force = false;
	
	@Option({"recursive", "r"})
	public void setRecursive()
	{
		_recursive = true;
	}
	
	@Option({"force", "f"})
	public void setForce()
	{
		_force = true;
	}

	public RmTool()
	{
		super(new FileResource(_DESCRIPTION), new FileResource(_USAGE), 
				false, ToolCategory.DATA);
		addManPage(new FileResource(_MANPAGE));
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		boolean recursive = _recursive;
		boolean force = _force;
		
		RNSPath path = RNSPath.getCurrent();
		int toReturn=0;
		for (int lcv = 0; lcv < numArguments(); lcv++)
		{
			GeniiPath gPath = new GeniiPath(getArgument(lcv));
			if(gPath.pathType() == GeniiPathType.Grid)
				rm(path, gPath.path(), recursive, force);
			else
			{
				File fPath = new File(gPath.path());
				toReturn+=rm(fPath, recursive, force);
			}
		}
			
		return toReturn;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() < 1)
			throw new InvalidToolUsageException();
	}
	
	private int rm(File path, boolean recursive, boolean force)
	{
		if ( ! path.exists())
		{
			if ( !force)
			{
				stderr.println(path.getName() + " does not exist.");
				return 1;
			}
			return 0;
		}
		if ((!recursive) && path.isDirectory())
		{
			if(force)
				return 1;
			stderr.println(path.getName() + " is a directory; Use -r to delete a local directory.");
			return 1;
		}
		if(path.isDirectory())
		{
			File[] files = path.listFiles();
			if (recursive)
			{
				for(File cur : files)
				{
					rm(cur, recursive, force);
				}
			}
			else
			{
				if (files.length != 0)
				{
					if (force)
						return 1;
					stderr.println(path.getName() + ": attempt to remove nonempty directory.");
					return 1;
				}
			}
		}
		boolean success = path.delete();
		if(!success && !force)
			return 1;
		return 0;
	}
	
	public void rm(RNSPath currentPath,
		String filePath, boolean recursive, boolean force)
		throws RNSException, IOException
	{
		for (RNSPath file : currentPath.expand(filePath))
			rm(file, recursive, force);
	}
	
	public void rm(RNSPath path, boolean recursive, 
		boolean force) throws RNSException
	{
		try
		{
			if (recursive)
				RNSUtilities.recursiveDelete(path);
			else{
				GeniiPath gPath = new GeniiPath(path.pwd());
				if (gPath.isDirectory()){
					if (path.listContents().size() > 0)
						throw new RNSException("Unable to delete a non-empty directory");
				}
	
				path.delete();
			}
				
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