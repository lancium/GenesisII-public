package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.gpath.GeniiPath;

public class CatTool extends BaseGridTool
{
	static final private String _DESCRIPTION =
		"Cats the indicated files to the stdout stream.";
	static final private String _USAGE =
		"cat <target-file0> ...";
	
	public CatTool()
	{
		super(_DESCRIPTION, _USAGE, false);
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		for (int lcv = 0; lcv < numArguments(); lcv++)
			cat(getArgument(lcv));
		
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
	}
	
	public void cat(String filePath)
		throws IOException
	{
		char []data = new char[ByteIOConstants.PREFERRED_SIMPLE_XFER_BLOCK_SIZE];
		int read;
		InputStream in = null;
		InputStreamReader reader = null;
		
		try
		{
			GeniiPath path = new GeniiPath(filePath);
			in = path.openInputStream();
			reader = new InputStreamReader(in);
			
			while ( (read = reader.read(data, 0, data.length)) > 0)
			{
				stdout.write(data, 0, read);
				stdout.flush();
			}
		}
		finally
		{
			StreamUtils.close(in);
		}
	}
}