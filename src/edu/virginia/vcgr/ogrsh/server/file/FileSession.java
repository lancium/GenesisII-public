package edu.virginia.vcgr.ogrsh.server.file;

import java.io.IOException;

public class FileSession
{
	private IFileDescriptor _desc;
	private int _referenceCount;

	public FileSession(IFileDescriptor desc)
	{
		_desc = desc;
		_referenceCount = 1;
	}

	synchronized public void addReference()
	{
		_referenceCount++;
	}

	synchronized public void removeReference() throws IOException
	{
		if (--_referenceCount <= 0)
			_desc.close();
	}

	public IFileDescriptor getDescriptor()
	{
		return _desc;
	}
}