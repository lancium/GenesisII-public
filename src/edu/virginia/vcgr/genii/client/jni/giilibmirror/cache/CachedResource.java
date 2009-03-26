package edu.virginia.vcgr.genii.client.jni.giilibmirror.cache;

import java.util.Date;

import edu.virginia.vcgr.genii.client.jni.giilibmirror.ResourceInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;

/** 
 * This abstract class is responsible for caching all information obtained for a resource
 */
public abstract class CachedResource {	
	protected RNSPath rnsPath = null;	
	protected Date lastAccessedTime = null;
	protected Date lastModifiedTime = null;
	protected Date createTime = null;	
	protected boolean isDirectory;
	protected boolean invalidated = false;
	protected long timeOfEntry;

	//Whether my information needs to be updated 
	private boolean dirty = false;	
	
	public CachedResource(){
		timeOfEntry = System.currentTimeMillis();
	}
	
	/** 
	 * Returns the RNSPath for this Resource
	 */
	public RNSPath getRNSPath() {
		return rnsPath;
	}
	
	/** 
	 * Gets information about the resource
	 */
	public abstract ResourceInformation getCachedInformation(int fileHandle);

	public boolean isDirectory() {
		return isDirectory;
	}
	
	public boolean isDirty() {
		return dirty;
	}

	public void setDirty(boolean dirty) {
		this.dirty = dirty;
	}
	
	public void setRNSPath(RNSPath newPath){
		synchronized(this){
			rnsPath = newPath;
		}
	}
	
	public boolean isValid(){
		return !invalidated;
	}

	public long getTimeOfEntry() {
		return timeOfEntry;
	}

	public void setTimeOfEntry(long timeOfEntry) {
		this.timeOfEntry = timeOfEntry;
	}
}
