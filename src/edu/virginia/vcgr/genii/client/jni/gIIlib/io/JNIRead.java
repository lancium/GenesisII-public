package edu.virginia.vcgr.genii.client.jni.gIIlib.io;

import edu.virginia.vcgr.genii.client.jni.gIIlib.io.file.IFSFile;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.file.IFSResource;

public class JNIRead {
	synchronized public static byte[] read(Integer fileHandle, Integer offset, Integer length){
		IFSResource resource = DataTracker.getInstance().getResource(fileHandle);
		byte[] toReturn = null;
		
		//Make sure the handle points to a valid file (not a directory)
		if(resource == null || resource.isDirectory()){
			System.out.println("Invalid file handle");						
		}
		else{					
			try{
				IFSFile file = (IFSFile)resource;
				file.lseek64(offset);
				toReturn = file.read(length);			
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return toReturn;
	}
}