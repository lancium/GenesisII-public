package edu.virginia.vcgr.genii.client.jni.gIIlib.io;

import edu.virginia.vcgr.genii.client.jni.gIIlib.io.file.WindowsIFSFile;


public class JNIWrite{
	public static Integer write(Integer fileHandle, byte[] data, Integer offset){
		WindowsIFSFile file = DataTracker.getInstance().getFile(fileHandle);
		int toReturn = -1;
		
		if(file == null){
			System.out.println("Invalid file handle");						
		}
		else{		
			try{
				file.lseek64(offset);
				toReturn = file.write(data);					
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return toReturn;
	}
	
	public static Integer truncateAppend(Integer fileHandle, String data, Integer offset){
		WindowsIFSFile file = DataTracker.getInstance().getFile(fileHandle);
		int toReturn = -1;

		if(file == null){
			System.out.println("Invalid file handle");						
		}
		else{
			try{
				toReturn = file.truncateAppend(offset.longValue(), data.getBytes());							
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return toReturn;
	}
}