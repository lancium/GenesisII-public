package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;

public class CopyTool extends BaseGridTool
{
	static private final String _DESCRIPTION =
		"Copies files around RNS space.";
	static private final String _USAGE =
		"cp <source-path> <target-path>";

	public CopyTool()
	{
		super(_DESCRIPTION, _USAGE, false);
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		copy(getArgument(0), getArgument(1));
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 2)
			throw new InvalidToolUsageException();
	}
	
	static private void copy(InputStream in, OutputStream out)
		throws IOException
	{
		StreamUtils.copyStream(in, out);
	}
	
	public static void copy(String sourcePath, String targetPath)
		throws FileNotFoundException, IOException
	{
		String sourceName = null;
		OutputStream out = null;
		InputStream in = null;
		
		try
		{
			GeniiPath source = new GeniiPath(sourcePath);
			if (!source.exists())
				throw new FileNotFoundException(String.format(
					"Unable to find source file %s!", source));
			if (!source.isFile())
				throw new IOException(String.format(
					"Source path %s is not a file!", source));
			
			int index = sourcePath.lastIndexOf('/');
			if (index >= 0)
				sourceName = sourcePath.substring(index + 1);
			else
				sourceName = sourcePath;
			
			GeniiPath target = new GeniiPath(targetPath);
			if (target.exists() && !target.isFile())
					target = new GeniiPath(String.format(
						"%s/%s", target, sourceName));

			in = source.openInputStream();
			out = target.openOutputStream();
			
			copy(in, out);
			out.flush();
		}
		finally
		{
			StreamUtils.close(in);
			StreamUtils.close(out);
		}
	}		
}