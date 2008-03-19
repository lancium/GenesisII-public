package edu.virginia.vcgr.genii.client.jni.gIIlib.io;

import edu.virginia.vcgr.genii.client.jni.gIIlib.io.file.IFSFile;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.file.IFSResource;


public class JNIWrite{
	public static Integer write(Integer fileHandle, byte[] data, Integer offset){
		IFSResource resource = DataTracker.getInstance().getResource(fileHandle);
		int toReturn = -1;
		
		//Make sure the handle points to a valid file (not a directory)
		if(resource == null || resource.isDirectory()){
			System.out.println("Invalid file handle");						
		}
		else{		
			try{
				IFSFile file = (IFSFile)resource;
				if(file.isStream() || offset != 0)
				{
					file.lseek64(offset);
				}
				toReturn = file.write(data);					
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return toReturn;
	}
	
	public static Integer truncateAppend(Integer fileHandle, String data, Integer offset){
		IFSResource resource = DataTracker.getInstance().getResource(fileHandle);
		int toReturn = -1;

		if(resource == null || resource.isDirectory()){
			System.out.println("Invalid file handle");						
		}
		else{
			try{
				IFSFile file = (IFSFile)resource;
				toReturn = file.truncateAppend(offset.longValue(), data.getBytes());							
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return toReturn;
	}
}