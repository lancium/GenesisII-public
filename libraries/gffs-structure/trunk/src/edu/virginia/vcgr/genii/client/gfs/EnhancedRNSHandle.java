package edu.virginia.vcgr.genii.client.gfs;

import java.io.IOException;
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ggf.rns.RNSEntryResponseType;

import edu.virginia.vcgr.fsii.DirectoryHandle;
import edu.virginia.vcgr.fsii.FilesystemStatStructure;
import edu.virginia.vcgr.fsii.exceptions.FSRuntimeException;
import edu.virginia.vcgr.genii.client.fuse.DirectoryManager;
import edu.virginia.vcgr.genii.client.fuse.MetadataManager;
import edu.virginia.vcgr.genii.client.rns.RNSIterable;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;

public class EnhancedRNSHandle implements DirectoryHandle
{
	private GenesisIIFilesystem _fs;
	private RNSIterable _entries;
	private String _rnsPathString;

	static private Log _logger = LogFactory.getLog(MetadataManager.class);

	public EnhancedRNSHandle(GenesisIIFilesystem fs, RNSIterable entries, String rnsPathString)
	{
		_fs = fs;
		_entries = entries;
		_rnsPathString = rnsPathString;
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
				String entryRNSPath = DirectoryManager.getPathForDirectoryEntry(_rnsPathString, next.getEntryName());
				FilesystemStatStructure statStructure = MetadataManager.retrieveStat(entryRNSPath);
				if (statStructure != null) {
					_logger.trace("Caching is working");
					return statStructure;
				}
				if (next.getEndpoint() != null) {
					return _fs.stat(next.getEntryName(), next.getEndpoint());
				}
				RNSPath currentPath = RNSPath.getCurrent();
				RNSPath entryPath = currentPath.lookup(entryRNSPath, RNSPathQueryFlags.MUST_EXIST);
				return _fs.stat(entryPath);
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