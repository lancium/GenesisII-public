package edu.virginia.vcgr.genii.client.gfs;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import edu.virginia.vcgr.fsii.DirectoryHandle;
import edu.virginia.vcgr.fsii.FilesystemStatStructure;
import edu.virginia.vcgr.fsii.exceptions.FSRuntimeException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;

public class DefaultRNSHandle implements DirectoryHandle
{
	private GenesisIIFilesystem _fs;
	private Collection<RNSPath> _entries;

	public DefaultRNSHandle(GenesisIIFilesystem fs, Collection<RNSPath> entries)
	{
		_fs = fs;
		_entries = entries;
	}

	@Override
	public Iterator<FilesystemStatStructure> iterator()
	{
		return new DefaultRNSIterator(_entries.iterator());
	}

	@Override
	public void close() throws IOException
	{
		// do nothing
	}

	private class DefaultRNSIterator implements Iterator<FilesystemStatStructure>
	{
		private Iterator<RNSPath> _entries;

		public DefaultRNSIterator(Iterator<RNSPath> entries)
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
			RNSPath next = _entries.next();
			if (next == null)
				return null;

			try {
				return _fs.stat(next);
			} catch (Exception e) {
				throw new FSRuntimeException(FSExceptions.translate("Unable to stat directory entry.", e));
			}
		}

		@Override
		public void remove()
		{
			_entries.remove();
		}
	}
}