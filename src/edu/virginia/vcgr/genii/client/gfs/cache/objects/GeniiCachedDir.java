package edu.virginia.vcgr.genii.client.gfs.cache.objects;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.virginia.vcgr.fsii.DirectoryHandle;
import edu.virginia.vcgr.fsii.FilesystemEntryType;
import edu.virginia.vcgr.fsii.FilesystemStatStructure;
import edu.virginia.vcgr.fsii.exceptions.FSEntryAlreadyExistsException;
import edu.virginia.vcgr.fsii.exceptions.FSEntryNotFoundException;
import edu.virginia.vcgr.fsii.exceptions.FSException;
import edu.virginia.vcgr.fsii.path.UnixFilesystemPathRepresentation;
import edu.virginia.vcgr.fsii.security.Permissions;
import edu.virginia.vcgr.genii.client.gfs.cache.handles.GeniiCachedDirectoryHandle;
import edu.virginia.vcgr.genii.client.gfs.cache.handles.GeniiOpenDirHandle;
import edu.virginia.vcgr.genii.client.gfs.cache.handles.GeniiOpenFileHandle;
import edu.virginia.vcgr.genii.client.gfs.cache.handles.GeniiOpenHandle;

/** 
 * This class is responsible for caching all information obtained from RNS for a directory
 * This class has optimized synchronization and assume that all dirHandles access this from 
 * different threads.  It assumes the common case for access to its directory entries are reads i.e.
 * reading the entries versus modifying the list.
 */
public class GeniiCachedDir extends GeniiCachedResource {			
	//Handles to entries for this directory (all items are cached!!!)
	private Hashtable<String, GeniiOpenHandle> directoryEntries;	
	
	static private Log _logger = LogFactory.getLog(GeniiCachedDir.class);
	
	public GeniiCachedDir(String[] path, Permissions permissions, boolean isMkDir)
			throws FSException {
		super(path);
		_logger.debug(String.format("Creating new cache item for %s", 
				UnixFilesystemPathRepresentation.INSTANCE.toString(_path)));
		if(isMkDir) {
			_fs.mkdir(path, permissions);
		} 
	}
			
	public synchronized DirectoryHandle listDirectory() throws FSException {
		if(directoryEntries == null) {
			refreshDirectoryEntries();
		}
		ArrayList<FilesystemStatStructure> fsStats = 
			new ArrayList<FilesystemStatStructure>();
		for(GeniiOpenHandle goh : directoryEntries.values()) {
			fsStats.add(goh.stat());
		}		
		return new GeniiCachedDirectoryHandle(fsStats);
	}
		
	public synchronized void refreshDirectoryEntries() throws FSException {
		_logger.debug(String.format("Refreshing entries for %s", 
				UnixFilesystemPathRepresentation.INSTANCE.toString(_path)));		
		if(directoryEntries != null) {
			merge();
		} else {
			directoryEntries = new Hashtable<String, GeniiOpenHandle>();		
			DirectoryHandle dh = _fs.listDirectory(_path);
			for(FilesystemStatStructure fsStat : dh) {
				_logger.debug(String.format("--Refresh found %s", 
						fsStat.getName()));
				GeniiOpenHandle goh;
				String[] childPath = Arrays.copyOf(_path, _path.length+1);			
				childPath[_path.length] = fsStat.getName();
				if(fsStat.getEntryType().equals(FilesystemEntryType.DIRECTORY)) {							
					goh = new GeniiOpenDirHandle(childPath);				
				} else {
					goh = new GeniiOpenFileHandle(childPath, null, null);
				}			
				directoryEntries.put(fsStat.getName(), goh);			
			}	
		}
	}
	
	private void merge() throws FSException {
		DirectoryHandle dh = _fs.listDirectory(_path);
		HashSet<String> names = new HashSet<String>();
		for(FilesystemStatStructure fsStat : dh) {			
			String name = fsStat.getName();
			names.add(name);			
			if(!directoryEntries.containsKey(name)){
				_logger.debug(String.format("--Merge adding %s", 
						name));
				GeniiOpenHandle goh;
				String[] childPath = Arrays.copyOf(_path, _path.length+1);			
				childPath[_path.length] = fsStat.getName();
				if(fsStat.getEntryType().equals(FilesystemEntryType.DIRECTORY)) {							
					goh = new GeniiOpenDirHandle(childPath);				
				} else {
					goh = new GeniiOpenFileHandle(childPath, null, null);
				}			
				directoryEntries.put(fsStat.getName(), goh);
			} 
		}
		ArrayList<String>namesToRemove = new ArrayList<String>();
		for(String name : directoryEntries.keySet()) {
			//No longer valid
			if(!names.contains(name)){
				_logger.debug(String.format("--Merge removing %s", 
						name));
				namesToRemove.add(name);
			}
		}
		for(String name : namesToRemove) {
			GeniiOpenHandle goh = removeEntry(name);
			if(!goh.isDirectory()){
				((GeniiOpenFileHandle)goh).close();
			}
		}
		
	}
	
	public synchronized void addEntry(String name, GeniiOpenHandle handle)
			throws FSEntryAlreadyExistsException{
		if(directoryEntries.containsKey(name)){
			throw new FSEntryAlreadyExistsException(String.format(
					"GeniiCached dir %s received duplicate add for %s", 
					UnixFilesystemPathRepresentation.INSTANCE.toString(_path),
					name));
		}
		directoryEntries.put(name, handle);
	}
	
	public synchronized GeniiOpenHandle removeEntry(String name)
			throws FSEntryNotFoundException {
		if(!directoryEntries.containsKey(name)){
			throw new FSEntryNotFoundException(String.format(
					"GeniiCached dir %s received remove for non existing child %s", 
					UnixFilesystemPathRepresentation.INSTANCE.toString(_path),
					name));
		}
		return directoryEntries.remove(name);		
	}

	@Override
	public void refresh() throws FSException{
		//Have stale entries
		if(directoryEntries != null) {
			refreshDirectoryEntries();
		}
		_statStructure = _fs.stat(_path);
	}

	@Override
	public boolean isDirectory() {		
		return true;	
	}

	@Override
	public synchronized void rename(String[] toPath) throws FSException {		
		setPath(toPath);
		_statStructure = null;
	}

	@Override
	public synchronized void invalidate() {
		invalidated = true;		
	}
}
