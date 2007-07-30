package edu.virginia.vcgr.genii.client.cmd.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.morgan.util.configuration.ConfigurationException;
import org.morgan.util.io.StreamUtils;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;
import edu.virginia.vcgr.genii.client.byteio.ByteIOInputStream;
import edu.virginia.vcgr.genii.client.byteio.ByteIOOutputStream;
import edu.virginia.vcgr.genii.client.cmd.InvalidToolUsageException;
import edu.virginia.vcgr.genii.client.cmd.ToolException;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSMultiLookupResultException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;

public class CopyTool extends BaseGridTool
{
	static private final String _DESCRIPTION =
		"Copies files around RNS space.";
	static private final String _USAGE =
		"cp [--local-src] [--local-dest] <source-path> <target-path>";

	private boolean _localSrc = false;
	private boolean _localDest = false;
	
	public CopyTool()
	{
		super(_DESCRIPTION, _USAGE, false);
	}
	
	public void setLocal_src()
	{
		_localSrc = true;
	}
	
	public void setLocal_dest()
	{
		_localDest = true;
	}
	
	@Override
	protected int runCommand() throws Throwable
	{
		copy(getArgument(0), _localSrc,
			getArgument(1), _localDest);
		return 0;
	}

	@Override
	protected void verify() throws ToolException
	{
		if (numArguments() != 2)
			throw new InvalidToolUsageException();
	}
	
	static private final int _BLOCK_SIZE = 
		ByteIOConstants.PREFERRED_SIMPLE_XFER_BLOCK_SIZE;
	
	static private void copy(InputStream in, OutputStream out)
		throws IOException
	{
		byte []data = new byte[_BLOCK_SIZE];
		int r;
		
		while ( (r = in.read(data)) >= 0)
		{
			out.write(data, 0, r);
		}
	}
	
	public void copy(String sourcePath, boolean isLocalSource,
		String targetPath, boolean isLocalTarget) 
		throws ConfigurationException, FileNotFoundException, IOException,
		RNSException
	{
		String sourceName = null;
		OutputStream out = null;
		InputStream in = null;
		
		RNSPath current = RNSPath.getCurrent();
		
		try
		{
			if (isLocalSource)
			{
				in = new FileInputStream(sourcePath);
				File sourceFile = new File(sourcePath);
				sourceName = sourceFile.getName();
			} else
			{
				RNSPath path = current.lookup(sourcePath, RNSPathQueryFlags.MUST_EXIST);
				in = new ByteIOInputStream(path);
				int index = sourcePath.lastIndexOf('/');
				if (index >= 0)
					sourceName = sourcePath.substring(index + 1);
				else
					sourceName = sourcePath;
			}
			
			if (isLocalTarget)
			{
				File targetFile = new File(targetPath);
				if (targetFile.exists() && targetFile.isDirectory())
					targetFile = new File(targetFile, sourceName);
				out = new FileOutputStream(targetFile);
			} else
			{
				RNSPath path = current.lookup(targetPath, RNSPathQueryFlags.DONT_CARE);
				if (path.exists() && path.isDirectory())
					path = path.lookup(sourceName, RNSPathQueryFlags.DONT_CARE);
				out = new ByteIOOutputStream(path);
			}
			
			copy(in, out);
		}
		catch (RNSPathDoesNotExistException e)
		{
			throw new FileNotFoundException(e.getMessage());
		}
		catch (RNSMultiLookupResultException e)
		{
			throw new IOException(e.getMessage());
		}
		finally
		{
			StreamUtils.close(in);
			StreamUtils.close(out);
		}
	}		
}