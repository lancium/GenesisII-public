package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;

public class FileRedirectionSink implements StreamRedirectionSink
{
	static final long serialVersionUID = 0L;

	private File _file;

	public FileRedirectionSink(File file)
	{
		_file = file;
	}

	@Override
	public OutputStream openSink(ExecutionContext context) throws IOException
	{
		return new FileOutputStream(_file);
	}
}