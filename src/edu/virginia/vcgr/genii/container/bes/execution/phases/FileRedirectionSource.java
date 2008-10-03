package edu.virginia.vcgr.genii.container.bes.execution.phases;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import edu.virginia.vcgr.genii.container.bes.execution.ExecutionContext;

public class FileRedirectionSource implements StreamRedirectionSource
{
	static final long serialVersionUID = 0L;
	
	private File _file;
	
	public FileRedirectionSource(File file)
	{
		_file = file;
	}
	
	@Override
	public InputStream openSource(ExecutionContext context) throws IOException
	{
		return new FileInputStream(_file);
	}
}