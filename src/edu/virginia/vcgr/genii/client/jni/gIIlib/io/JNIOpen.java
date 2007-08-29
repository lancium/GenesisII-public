package edu.virginia.vcgr.genii.client.jni.gIIlib.io;

import java.io.InputStream;
import java.io.OutputStream;

import edu.virginia.vcgr.genii.client.byteio.ByteIOInputStream;
import edu.virginia.vcgr.genii.client.byteio.ByteIOOutputStream;
import edu.virginia.vcgr.genii.client.jni.gIIlib.JNILibraryBase;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;

public class JNIOpen extends JNILibraryBase{
	
	public static Integer open(String fileName, Boolean create, Boolean read, 
			Boolean write){
		int fileHandle = fileName.hashCode();
		InputStream theRData = null;
		OutputStream theWData = null;
		
		if(read){		
			theRData = DataTracker.getInstance().getReadStream(fileHandle);
		}
		if(write){
			theWData = DataTracker.getInstance().getWriteStream(fileHandle);						
		}
				
		try{
			if(theRData != null || theWData != null){
				System.out.println("File already open");			
			}
			else{
				tryToInitialize();
				
				RNSPath current = RNSPath.getCurrent();
				RNSPath filePath; 
				if(create){
					filePath =  current.lookup(fileName, RNSPathQueryFlags.DONT_CARE);									
				}
				else{
					filePath = current.lookup(fileName, RNSPathQueryFlags.MUST_EXIST);
				}
				if(write){
					theWData = new ByteIOOutputStream(filePath);
					DataTracker.getInstance().putStream(fileHandle, theWData);
				}
				if(read){
					theRData = new ByteIOInputStream(filePath);
					DataTracker.getInstance().putStream(fileHandle, theRData);
				}								
			}
			return fileHandle;	
		}
		catch(Exception e){
			e.printStackTrace();
			DataTracker.getInstance().removeStream(fileHandle);
			return -1;						
		}				
	}
}
