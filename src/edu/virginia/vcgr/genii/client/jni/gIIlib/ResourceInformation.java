package edu.virginia.vcgr.genii.client.jni.gIIlib;

import java.util.ArrayList;
import java.util.Date;

public class ResourceInformation {
	public boolean isDirectory;
	public Integer resourceHandle;	
	public Date lastAccessedTime;
	public Date lastModifiedTime;
	public Date createTime;
	
	// For directories, num entries excluding . and ..
	public long size;
	public String name;
	
	public ResourceInformation(boolean isDir, String name, int handle, 
			Date at, Date mt, Date ct, long size){
		
		this.name = name;
		isDirectory = isDir;
		resourceHandle = handle;
		lastAccessedTime = at;
		lastModifiedTime = mt;
		createTime = ct;
		this.size = size;
	}
	
	public ArrayList<String> convertForJNI(){
		ArrayList<String> toReturn = new ArrayList<String>();
		
		toReturn.add(resourceHandle.toString());
		toReturn.add(isDirectory ? "D" : "F");
		toReturn.add(String.valueOf(size));
		toReturn.add(name);

		return toReturn;
	}
}
