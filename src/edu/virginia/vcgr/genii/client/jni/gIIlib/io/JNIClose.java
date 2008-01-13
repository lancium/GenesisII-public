package edu.virginia.vcgr.genii.client.jni.gIIlib.io;

import edu.virginia.vcgr.genii.client.jni.gIIlib.io.file.WindowsIFSFile;


public class JNIClose {
	public static Boolean close(Integer fileHandle){		
		WindowsIFSFile file = DataTracker.getInstance().getFile(fileHandle);
		
		if(file == null){
			System.out.println("Directory being closed or invalid FH");			
			return false;
		}
		else{
			try{
				file.close();
			}catch(Exception e){
				e.printStackTrace();
			}
			DataTracker.getInstance().removeFile(fileHandle);			
			return true;
		}				
	}
}
