package edu.virginia.vcgr.fsii;

import java.io.Closeable;
import java.util.Arrays;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class FileHandleTable<FileObjectType>
{
	static private Log _logger = LogFactory.getLog(FileHandleTable.class);

	private Object[] _table;
	private int _nextFree;

	public FileHandleTable(int maximumSize)
	{
		_table = new Object[maximumSize];
		Arrays.fill(_table, null);

		_nextFree = 0;
	}

	synchronized public int allocate(FileObjectType file)
	{
		if (file == null)
			throw new RuntimeException("Not allowed to allocate a file " + "table entry for a null file object.");
		if (file instanceof Integer)
			throw new RuntimeException("Not allowed to allocate a file " + "table entry for a file object of type Integer");

		if (_nextFree >= _table.length) {
			_logger.error("FileHandleTable is full -- error.");
			return -1;
		}

		int ret = _nextFree;

		Object entry = _table[_nextFree];
		if (entry == null)
			_nextFree++;
		else
			_nextFree = Integer.class.cast(entry);

		if (_logger.isDebugEnabled())
			_logger.debug(String.format("FileHandleTable[%x] -- Allocating slot %d, next free is %d.", this.hashCode(), ret,
				_nextFree));

		_table[ret] = file;
		return ret;
	}

	@SuppressWarnings("unchecked")
	synchronized public FileObjectType get(int handle)
	{
		Object entry = _table[handle];
		if (entry instanceof Integer)
			return null;

		return (FileObjectType) entry;
	}

	synchronized public void release(int handle)
	{
		Object entry = _table[handle];
		if (entry instanceof Integer)
			return;

		if (entry != null && (entry instanceof Closeable)) {
			try {
				if (_logger.isDebugEnabled())
					_logger.debug(String.format("Closing instance of %s.", entry.getClass().getName()));
				((Closeable) entry).close();
				if (_logger.isDebugEnabled())
					_logger.debug(String.format("Instance of %s closed.", entry.getClass().getName()));
			} catch (Throwable cause) {
				_logger.warn(String.format("Unable to close instance of %s.", entry.getClass().getName()), cause);
			}
		} else {
			if (_logger.isDebugEnabled())
				_logger.debug(String.format("Releasing instance of %s without closing.", entry == null ? "<null>" : entry
					.getClass().getName()));
		}

		if (_logger.isDebugEnabled())
			_logger.debug(String.format("FileHandleTable[%x] -- Released handle %d and pointing that slot to %d.",
				this.hashCode(), handle, _nextFree));
		_table[handle] = new Integer(_nextFree);
		_nextFree = handle;
	}
}