package edu.virginia.vcgr.genii.client.gfs;

import java.io.IOException;
import java.util.Iterator;

import org.ggf.rns.RNSEntryResponseType;

import edu.virginia.vcgr.fsii.DirectoryHandle;
import edu.virginia.vcgr.fsii.FilesystemStatStructure;
import edu.virginia.vcgr.fsii.exceptions.FSRuntimeException;
import edu.virginia.vcgr.genii.client.rns.RNSIterable;

public class EnhancedRNSHandle implements DirectoryHandle
{
	private GenesisIIFilesystem _fs;
	private RNSIterable _entries;

	public EnhancedRNSHandle(GenesisIIFilesystem fs, RNSIterable entries)
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
	}

	private class EnhancedRNSIterator implements Iterator<FilesystemStatStructure>
	{
		private Iterator<RNSEntryResponseType> _entries;

		public EnhancedRNSIterator(Iterator<RNSEntryResponseType> entries)
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
			RNSEntryResponseType next = _entries.next();
			if (next == null)
				return null;

			try {
				return _fs.stat(next.getEntryName(), next.getEndpoint());
			} catch (Exception e) {
				throw new FSRuntimeException(FSExceptions.translate("Unable to stat entry.", e));
			}
		}

		@Override
		public void remove()
		{
			_entries.remove();
		}
	}
}