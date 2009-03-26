package edu.virginia.vcgr.genii.client.jni.giilibmirror.io.file;

import edu.virginia.vcgr.genii.client.rns.RNSPath;
import java.io.Closeable;

public abstract class IFSResource implements Closeable{
	protected RNSPath myPath;
	protected boolean isDirectory; 
	
	public IFSResource(){
		myPath = null;
	}
	
	public boolean isDirectory(){
		return isDirectory;
	}
	
	public IFSResource(RNSPath path){
		myPath =  path;
	}
	
	public RNSPath getRNSPath(){
		return myPath;
	}
}
