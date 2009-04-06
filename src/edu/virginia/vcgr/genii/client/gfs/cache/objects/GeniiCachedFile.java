package edu.virginia.vcgr.genii.client.gfs.cache.objects;

import java.nio.ByteBuffer;
import java.util.ArrayList;

import edu.virginia.vcgr.fsii.FilesystemStatStructure;
import edu.virginia.vcgr.fsii.exceptions.FSException;
import edu.virginia.vcgr.fsii.file.OpenFlags;
import edu.virginia.vcgr.fsii.file.OpenModes;
import edu.virginia.vcgr.fsii.security.Permissions;
import edu.virginia.vcgr.genii.client.gfs.cache.GeniiCacheGenericFileObject;
import edu.virginia.vcgr.genii.client.gfs.cache.GeniiCacheManager;
import edu.virginia.vcgr.genii.client.gfs.cache.handles.GeniiOpenFileHandle;

/** 
 * This class is responsible for caching all information obtained from RNS for a file
 */
public class GeniiCachedFile extends GeniiCachedResource implements GeniiCacheGenericFileObject {
	
	boolean dirty = false;
	
	private long _fileHandle;			
	
	OpenFlags _flags;
	
	OpenModes _currentMode;

	//A list of handles that are referencing me
	private ArrayList <GeniiOpenFileHandle> myHandles = new ArrayList<GeniiOpenFileHandle>();	
	
	public GeniiCachedFile(String[] path, OpenFlags flags, OpenModes modes, 
			Permissions permissions) throws FSException {
		super(path);
		_fs = GeniiCacheManager.getInstance().get_fs();		
		_flags = flags;
		_currentMode = modes;		
		_fileHandle = _fs.open(path, flags, modes, permissions);		
	}
		
	/**
	 * Attach file handle to this cached file
	 */
	public synchronized void attach(GeniiOpenFileHandle fh) throws FSException {
		if(!invalidated) {
			//New permissions are stronger
			if(fh.getMode() == OpenModes.READ_WRITE &&
					(_currentMode == null || OpenModes.READ == _currentMode)){								
				long newFileID = _fs.open(_path, _flags, fh.getMode(), null);
				_fs.close(_fileHandle);
				_fileHandle = newFileID;
				_currentMode = fh.getMode();
			} 
			
			if(fh.getFlags().isTruncate()) { 
				_fs.truncate(_path, 0);				
				_fs.flush(_fileHandle);
				_statStructure = new FilesystemStatStructure(
						_statStructure.getINode(),
						_statStructure.getName(),
						_statStructure.getEntryType(),
						0,
						_statStructure.getCreated(),
						_statStructure.getLastModified(),
						_statStructure.getLastAccessed(),
						_statStructure.getPermissions()				
					);									
			}			
			synchronized(myHandles){
				myHandles.add(fh);
			}
			fh.attach(this);
		} else {
			throw new FSException("GeniiCachedDir accessed after invalidation");
		}
	}
	
	public synchronized void detatch(GeniiOpenFileHandle fh){
		myHandles.remove(fh);		
	}
	
	/** 
	 * Invalidates File Handles that are referring to this entry
	 * @param warnOnValidate
	 */
	public synchronized void invalidate(){		
		invalidated = true;			
		for(GeniiOpenFileHandle fh : myHandles)
		{			
			fh.invalidate();
		}
		myHandles.clear();		
	}	
	
	@Override
	public synchronized void refresh() throws FSException {		
		flush();						
		_statStructure = _fs.stat(_path);
	}

	@Override
	public synchronized void close() throws FSException {		
		_fs.close(_fileHandle);		
	}

	@Override
	public synchronized void flush() throws FSException {
		if(dirty) {
			_fs.flush(_fileHandle);
			dirty = false;
		}		
	}

	@Override
	public synchronized void read(long offset, ByteBuffer target) throws FSException {
		_fs.read(_fileHandle, offset, target);		
	}

	@Override
	public synchronized void write(long offset, ByteBuffer source) throws FSException {
		dirty = true;		
		_fs.write(_fileHandle, offset, source);
		long newLength = offset + source.position();
		if(newLength > _statStructure.getSize()) {
			_statStructure = new FilesystemStatStructure(
					_statStructure.getINode(),
					_statStructure.getName(),
					_statStructure.getEntryType(),
					newLength,
					_statStructure.getCreated(),
					_statStructure.getLastModified(),
					_statStructure.getLastAccessed(),
					_statStructure.getPermissions()				
				);	
		}
	}

	@Override
	public boolean isDirectory() {
		return false;
	}

	@Override
	public synchronized void rename(String[] toPath) throws FSException {
		flush();
		close();
		_fs.rename(_path, toPath);
		_fileHandle = _fs.open(toPath, _flags, _currentMode, null);		
		setPath(toPath);
	}

	@Override
	public synchronized void truncate(long newSize) throws FSException {
		_fs.truncate(_path, newSize);
		_statStructure = new FilesystemStatStructure(
				_statStructure.getINode(),
				_statStructure.getName(),
				_statStructure.getEntryType(),
				newSize,
				_statStructure.getCreated(),
				_statStructure.getLastModified(),
				_statStructure.getLastAccessed(),
				_statStructure.getPermissions()				
			);				
	}
}
