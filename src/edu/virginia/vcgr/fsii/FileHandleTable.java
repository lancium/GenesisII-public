package edu.virginia.vcgr.fsii;

import java.io.Closeable;
import java.util.Arrays;

public class FileHandleTable<FileObjectType>
{
	private Object []_table;
	private int _nextFree;
	
	public FileHandleTable(int maximumSize)
	{
		_table = new Object[maximumSize];
		Arrays.fill(_table, null);
		
		_nextFree = 0;
	}
	
	public int allocate(FileObjectType file)
	{
		if (_nextFree >= _table.length)
			return -1;
		
		int ret = _nextFree;
		
		Object entry = _table[_nextFree];
		if (entry == null)
			_nextFree++;
		else
			_nextFree = Integer.class.cast(entry);
		
		_table[ret] = file;
		return ret;
	}
	
	@SuppressWarnings("unchecked")
	public FileObjectType get(int handle)
	{
		Object entry = _table[handle];
		if (entry instanceof Integer)
			return null;
		
		return (FileObjectType)entry;
	}
	
	public void release(int handle)
	{
		Object entry = _table[handle];
		if (entry instanceof Integer)
			return;
		
		if (entry != null && (entry instanceof Closeable))
		{
			try { ((Closeable)entry).close(); } catch (Throwable cause) {}
		}
		
		_table[handle] = new Integer(_nextFree);
		_nextFree = handle;
	}
}