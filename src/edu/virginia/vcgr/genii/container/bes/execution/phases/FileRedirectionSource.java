package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionException;

public class FileRedirectionSource implements StreamRedirectionSource
{
	static final long serialVersionUID = 0L;
	
	private String _filename;
	
	public FileRedirectionSource(String filename)
	{
		_filename = filename;
	}
	
	@Override
	public InputStream openSource(ExecutionContext context) throws IOException
	{
		try
		{
			return new FileInputStream(new File(
				context.getCurrentWorkingDirectory(), _filename));
		}
		catch (ExecutionException ee)
		{
			throw new IOException("Unable to open redirect source.", ee);
		}
	}
}