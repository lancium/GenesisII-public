package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;
import edu.virginia.vcgr.genii.client.gpath.GeniiPathType;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;

public class MoveTool extends BaseGridTool
{
	static final private String DESCRIPTION = "edu/virginia/vcgr/genii/client/cmd/tools/description/dmv";
	static final private String USAGE = "edu/virginia/vcgr/genii/client/cmd/tools/usage/umv";
	static final private String _MANPAGE = "edu/virginia/vcgr/genii/client/cmd/tools/man/mv";

	static private int moveLocal(String source, String target) throws IOException
	{
		File sourceFile = new File(source);
		if (!sourceFile.exists())
			throw new FileNotFoundException(String.format("Path %s does not exist.", source));

		File targetFile = new File(target);
		if (!sourceFile.renameTo(targetFile))
			throw new IOException(String.format("Unable to move local file %s to %s.", source, target));

		return 0;
	}

	static private int moveGrid(String source, String target) throws RNSException
	{
		RNSPath current = RNSPath.getCurrent();
		RNSPath sourcePath = current.lookup(source, RNSPathQueryFlags.MUST_EXIST);
		RNSPath targetPath = current.lookup(target, RNSPathQueryFlags.MUST_NOT_EXIST);

		targetPath.link(sourcePath.getEndpoint());

		try {
			sourcePath.unlink();
			sourcePath = null;
		} finally {
			if (sourcePath != null)
				targetPath.unlink();
		}

		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 2)
			throw new InvalidToolUsageException("Incorrect number of arguments.");
	}

	@Override
	protected int runCommand() throws Throwable
	{
		GeniiPath source = new GeniiPath(getArgument(0));
		GeniiPath target = new GeniiPath(getArgument(1));

		if (source.pathType() != target.pathType())
			throw new InvalidToolUsageException(
				"The source and target must both be local paths, or they must both be grid paths.");

		if (source.pathType() == GeniiPathType.Local)
			return moveLocal(source.path(), target.path());
		else
			return moveGrid(source.path(), target.path());
	}

	public MoveTool()
	{
		super(new FileResource(DESCRIPTION), new FileResource(USAGE), false, ToolCategory.DATA);
		addManPage(new FileResource(_MANPAGE));
	}
}