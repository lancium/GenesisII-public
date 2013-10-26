package edu.virginia.vcgr.appmgr.patch.builder.planning;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarOutputStream;

import edu.virginia.vcgr.appmgr.io.IOUtils;

public class PatchPlanContext implements Closeable
{
	private File _sourceDirectory;
	private FileOutputStream _fos = null;
	private JarOutputStream _jos = null;

	@Override
	protected void finalize() throws Throwable
	{
		close();
	}

	public PatchPlanContext(File sourceDirectory, File targetFile) throws IOException
	{
		_sourceDirectory = sourceDirectory;
		_jos = new JarOutputStream(_fos = new FileOutputStream(targetFile));
	}

	public File getSourceFile(String entryName)
	{
		return new File(_sourceDirectory, entryName);
	}

	public JarOutputStream getJarOutputStream()
	{
		return _jos;
	}

	@Override
	synchronized public void close() throws IOException
	{
		IOUtils.close(_jos);
		IOUtils.close(_fos);

		_jos = null;
		_fos = null;
	}
}