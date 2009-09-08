package edu.virginia.vcgr.genii.ui.persist;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

final public class PersistenceKey
{
	private File _persistenceFile;
	
	PersistenceKey(File persistenceFile)
	{
		_persistenceFile = persistenceFile;
	}
	
	File persistenceFile()
	{
		return _persistenceFile;
	}
	
	final public InputStream open() throws IOException
	{
		return new FileInputStream(_persistenceFile);
	}
	
	final public boolean equals(PersistenceKey other)
	{
		return _persistenceFile.getAbsoluteFile().equals(
			other._persistenceFile.getAbsoluteFile());
	}
	
	@Override
	final public boolean equals(Object other)
	{
		if (other instanceof PersistenceKey)
			return equals((PersistenceKey)other);
		
		return false;
	}
	
	@Override
	final public int hashCode()
	{
		return _persistenceFile.getAbsoluteFile().hashCode();
	}
	
	@Override
	final public String toString()
	{
		return _persistenceFile.toString();
	}
}