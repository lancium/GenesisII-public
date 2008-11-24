package edu.virginia.vcgr.genii.client.gfs;

import java.io.IOException;
import java.util.Iterator;

import org.ggf.rns.EntryType;

import edu.virginia.vcgr.fsii.DirectoryHandle;
import edu.virginia.vcgr.fsii.FilesystemStatStructure;
import edu.virginia.vcgr.fsii.exceptions.FSRuntimeException;
import edu.virginia.vcgr.genii.client.iterator.WSIterable;

public class EnhancedRNSHandle implements DirectoryHandle
{
	private GenesisIIFilesystem _fs;
	private WSIterable<EntryType> _entries;
	
	public EnhancedRNSHandle(GenesisIIFilesystem fs, 
		WSIterable<EntryType> entries)
	{
		_fs = fs;
		_entries = entries;
	}
	
	@Override
	public Iterator<FilesystemStatStructure> iterator()
	{
		return new EnhancedRNSIterator(_entries.iterator());
	}

	@Override
	public void close() throws IOException
	{
		_entries.close();
	}
	
	private class EnhancedRNSIterator 
		implements Iterator<FilesystemStatStructure>
	{
		private Iterator<EntryType> _entries;
		
		public EnhancedRNSIterator(Iterator<EntryType> entries)
		{
			_entries = entries;
		}
		
		@Override
		public boolean hasNext()
		{
			return _entries.hasNext();
		}

		@Override
		public FilesystemStatStructure next()
		{
			EntryType next = _entries.next();
			if (next == null)
				return null;
			
			try
			{
				return _fs.stat(next.getEntry_name(), 
					next.getEntry_reference());
			}
			catch (Exception e)
			{
				throw new FSRuntimeException(FSExceptions.translate(
					"Unable to stat entry.", e));
			}
		}

		@Override
		public void remove()
		{
			_entries.remove();
		}
	}
}