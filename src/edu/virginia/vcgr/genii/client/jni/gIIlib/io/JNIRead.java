package edu.virginia.vcgr.genii.client.jni.gIIlib.io;

import edu.virginia.vcgr.genii.client.jni.gIIlib.io.file.WindowsIFSFile;

public class JNIRead {
	synchronized public static byte[] read(Integer fileHandle, Integer offset, Integer length){
		WindowsIFSFile file = DataTracker.getInstance().getFile(fileHandle);
		byte[] toReturn = null;
		
		if(file == null){
			System.out.println("Invalid file handle");						
		}
		else{		
			try{
				file.lseek64(offset);
				toReturn = file.read(length);			
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return toReturn;
	}
}