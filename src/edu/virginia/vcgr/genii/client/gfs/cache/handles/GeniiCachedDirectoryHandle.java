package edu.virginia.vcgr.genii.client.gfs.cache.handles;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import edu.virginia.vcgr.fsii.DirectoryHandle;
import edu.virginia.vcgr.fsii.FilesystemStatStructure;

public class GeniiCachedDirectoryHandle implements DirectoryHandle {
	ArrayList<FilesystemStatStructure> _entries;
	
	public GeniiCachedDirectoryHandle(ArrayList<FilesystemStatStructure> entries){		
		this._entries = entries;
	}

	@Override
	public Iterator<FilesystemStatStructure> iterator() {
		return _entries.iterator();
	}

	@Override
	public void close() throws IOException {
		// Do nothing		
	}
}
