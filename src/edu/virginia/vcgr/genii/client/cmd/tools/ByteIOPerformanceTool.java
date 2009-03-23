package edu.virginia.vcgr.genii.client.cmd.tools;

import java.util.Collection;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.io.FileResource;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;

public class ByteIOPerformanceTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"Run a test to determine the create-file time for exports.";
	static final private FileResource _USAGE =
		new FileResource("edu/virginia/vcgr/genii/client/cmd/tools/resources/byteioperf-usage.txt");
	
	static private void makeCorrectFileCount(RNSPath target, int fileCount)
		throws RNSPathDoesNotExistException, RNSException
	{
		RNSPath newFile;
		Collection<RNSPath> files = target.listContents();
		if (files.size() < fileCount)
		{
			System.err.format("Growing directory from %d files to %d.\n",
				files.size(), fileCount);
			for (int lcv = files.size(); lcv < fileCount; lcv++)
			{
				newFile = target.lookup(String.format("%s/placeholder.%d",
					target.pwd(), lcv), RNSPathQueryFlags.MUST_NOT_EXIST);
				newFile.createNewFile();
			}
		} else
		{
			System.err.format("Shrinking directory from %d files to %d.\n",
				files.size(), fileCount);
			for (int lcv = files.size() - 1; lcv >= fileCount; lcv--)
			{
				newFile = target.lookup(String.format("placeholder.%d",lcv), 
					RNSPathQueryFlags.MUST_EXIST);
				newFile.delete();
			}
		}
	}
	
	static private void doFile(RNSPath dir) 
		throws RNSException
	{
		RNSPath newFile;
		
		System.err.format("Testing.\n");
		newFile = dir.lookup("test-file.dat",
			RNSPathQueryFlags.MUST_NOT_EXIST);
		newFile.createNewFile();
		newFile.delete();
	}
	
	public ByteIOPerformanceTool()
	{
		super(_DESCRIPTION, _USAGE, true);
	}

	@Override
	protected int runCommand() throws Throwable
	{
		RNSPath targetDir = RNSPath.getCurrent().lookup(getArgument(0),
			RNSPathQueryFlags.MUST_EXIST);
		int iterations = Integer.parseInt(getArgument(1));
		
		for (int lcv = 2; lcv < numArguments(); lcv++)
		{
			int fileCount = Integer.parseInt(getArgument(lcv));
			makeCorrectFileCount(targetDir, fileCount);
			for (int iter = 0; iter < iterations; iter++)
				doFile(targetDir);
		}
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() < 3)
			throw new InvalidToolUsageException();
	}
}