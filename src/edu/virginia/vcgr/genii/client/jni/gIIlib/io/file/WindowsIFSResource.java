package edu.virginia.vcgr.genii.client.jni.gIIlib.io.file;

import edu.virginia.vcgr.genii.client.rns.RNSPath;
import java.io.Closeable;

public abstract class WindowsIFSResource implements Closeable{
	protected RNSPath myPath;
	
	public WindowsIFSResource(){
		myPath = null;
	}
	
	public WindowsIFSResource(RNSPath path){
		myPath =  path;
	}
}
