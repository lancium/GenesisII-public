package edu.virginia.vcgr.genii.container.bes.jsdl.personality.common;

import edu.virginia.vcgr.genii.client.jsdl.FilesystemManager;
import edu.virginia.vcgr.genii.client.jsdl.FilesystemRelativePath;
import edu.virginia.vcgr.genii.client.jsdl.JSDLException;

public class StringOrPath
{
	private String _string = null;
	private FilesystemRelativePath _path = null;
	
	public StringOrPath(String str)
	{
		_string = str;
	}
	
	public StringOrPath(FilesystemRelativePath path)
	{
		_path = path;
	}
	
	public String toString(FilesystemManager fsManager)
		throws JSDLException
	{
		if (_string != null)
			return _string;
		
		return fsManager.lookup(_path).getAbsolutePath();
	}
}