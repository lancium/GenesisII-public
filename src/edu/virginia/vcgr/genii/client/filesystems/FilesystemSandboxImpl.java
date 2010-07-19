package edu.virginia.vcgr.genii.client.filesystems;

import java.io.File;
import java.io.FileNotFoundException;

class FilesystemSandboxImpl implements FilesystemSandbox
{
	private File _sandboxRoot;
	
	FilesystemSandboxImpl(File sandboxRoot) throws FileNotFoundException
	{
		if (!sandboxRoot.exists())
			sandboxRoot.mkdirs();
		if (!sandboxRoot.exists())
			throw new FileNotFoundException(String.format(
				"Unable to locate sandbox root at \"%s\".", sandboxRoot));
		else if (!sandboxRoot.isDirectory())
			throw new FileNotFoundException(String.format(
					"Unable to locate sandbox root at \"%s\".", sandboxRoot));
		
		_sandboxRoot = sandboxRoot;
	}
	
	@Override
	final public File sandboxRoot()
	{
		return _sandboxRoot;
	}
}