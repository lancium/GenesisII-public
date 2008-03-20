package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;
import edu.virginia.vcgr.genii.container.bes.execution.ExecutionException;

public class FileRedirectionSink implements StreamRedirectionSink
{
	static final long serialVersionUID = 0L;
	
	private String _filename;
	
	public FileRedirectionSink(String filename)
	{
		_filename = filename;
	}
	
	@Override
	public OutputStream openSink(ExecutionContext context) throws IOException
	{
		try
		{
			return new FileOutputStream(new File(
				context.getCurrentWorkingDirectory(), _filename));
		}
		catch (ExecutionException ee)
		{
			throw new IOException("Unable to open redirect sink.", ee);
		}
	}
}