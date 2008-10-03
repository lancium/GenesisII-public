package edu.virginia.vcgr.genii.client.jsdl;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class FilesystemManager implements Serializable
{
	static final long serialVersionUID = 0L;
	
	private Map<String, JSDLFileSystem> _filesystems =
		new HashMap<String, JSDLFileSystem>();
	
	private File _workingDirectory = null;
	
	public void setWorkingDirectory(File workingDirectory)
	{
		_workingDirectory = workingDirectory;
	}
	
	public void addFilesystem(String filesystemName, JSDLFileSystem filesystem)
	{
		if (filesystemName == null)
			throw new IllegalArgumentException("FilesytemName cannot be null.");
		
		if (filesystem == null)
			throw new IllegalArgumentException("Filesystem cannot be null.");
		
		_filesystems.put(filesystemName, filesystem);
	}
	
	public File lookup(FilesystemRelativePath path)
		throws JSDLException
	{
		if (path == null)
			return null;
		
		String fsName = path.getFileSystemName();
		String relativePath = path.getString();
		
		if (relativePath.startsWith("/") || relativePath.startsWith("\\"))
			return new File(relativePath);
		
		if (fsName == null)
		{
			if (_workingDirectory == null)
				return new File(relativePath);
			else
				return new File(_workingDirectory, relativePath);
		} else
		{
			JSDLFileSystem fs = _filesystems.get(fsName);
			if (fs == null)
				throw new JSDLException(String.format(
					"Couldn't locate file system \"%s\".", fsName));
			
			try
			{
				return fs.relativeTo(relativePath);
			}
			catch (IOException ioe)
			{
				throw new JSDLException(
					"Unable to look up path relative to file system.", ioe);
			}
		}
	}
	
	public void releaseAll()
	{
		for (JSDLFileSystem fs : _filesystems.values())
			fs.release();
	}
}