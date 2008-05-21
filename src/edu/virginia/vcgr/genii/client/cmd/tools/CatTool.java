package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.IOException;
import java.io.InputStream;

import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.byteio.ByteIOStreamFactory;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;

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
		RNSPath path = RNSPath.getCurrent();
		for (int lcv = 0; lcv < numArguments(); lcv++)
		{
			cat(path, getArgument(lcv));
		}
		
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
	}
	
	public void cat(RNSPath currentPath,
		String filePath)
		throws RNSException, ConfigurationException, IOException
	{
		for (RNSPath file : currentPath.expand(filePath))		
			cat(file);
	}
	
	 public void cat(RNSPath file)
		throws RNSException, ConfigurationException, IOException
	{
		byte []data = new byte[ByteIOConstants.PREFERRED_SIMPLE_XFER_BLOCK_SIZE];
		int read;
		TypeInformation typeInfo = new TypeInformation(file.getEndpoint());
		if (!typeInfo.isByteIO())
			throw new RNSException("Path \"" + file.pwd() +
				"\" is not a file.");
		
		InputStream in = null;
		
		try
		{
			in = ByteIOStreamFactory.createInputStream(file);
			while ( (read = in.read(data)) >= 0)
				stdout.write(data, 0, read);
		}
		finally
		{
			StreamUtils.close(in);
		}
	}
}