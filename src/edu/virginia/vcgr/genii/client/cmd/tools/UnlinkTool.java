package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.File;
import java.io.IOException;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.gpath.GeniiPathType;
import edu.virginia.vcgr.genii.client.io.FileResource;

public class UnlinkTool extends BaseGridTool
{
	static final private String _DESCRIPTION = "edu/virginia/vcgr/genii/client/cmd/tools/description/dunlink";
	static final private String _USAGE = "edu/virginia/vcgr/genii/client/cmd/tools/usage/uunlink";
	static final private String _MANPAGE = "edu/virginia/vcgr/genii/client/cmd/tools/man/unlink";

	public UnlinkTool()
	{
		super(new FileResource(_DESCRIPTION), new FileResource(_USAGE), false, ToolCategory.DATA);
		addManPage(new FileResource(_MANPAGE));
	}

	@Override
	protected int runCommand() throws Throwable
	{
		RNSPath path = RNSPath.getCurrent();
		int toReturn = 0;
		for (int lcv = 0; lcv < numArguments(); lcv++) {
			GeniiPath gPath = new GeniiPath(getArgument(lcv));
			if (gPath.pathType() == GeniiPathType.Local) {
				File fPath = new File(gPath.path());
				toReturn += unlink(fPath);
			} else
				unlink(path, new GeniiPath(getArgument(lcv)).path());
		}

		return toReturn;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() < 1)
			throw new InvalidToolUsageException();
	}

	static public void unlink(RNSPath currentPath, String filePath) throws RNSException, IOException
	{
		RNSPath file = currentPath.lookup(filePath, RNSPathQueryFlags.MUST_EXIST);

		file.unlink();
	}

	private int unlink(File path) throws RNSPathDoesNotExistException
	{
		if (!path.exists()) {
			throw new RNSPathDoesNotExistException(path.getName());
		}
		boolean success = path.delete();
		if (!success) {
			stderr.println(path.getName() + " failed to delete");
			return 1;
		}
		return 0;
	}
}