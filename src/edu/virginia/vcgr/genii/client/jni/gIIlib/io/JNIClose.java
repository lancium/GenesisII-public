package edu.virginia.vcgr.genii.client.jni.gIIlib.io;

import java.io.InputStream;
import java.io.OutputStream;

import org.morgan.util.io.StreamUtils;


public class JNIClose {
	public static Boolean close(Integer fileHandle){		
		InputStream theRData = DataTracker.getInstance().getReadStream(fileHandle);
		OutputStream theWData = DataTracker.getInstance().getWriteStream(fileHandle);			
		
		if(theRData == null && theWData == null){
			System.out.println("Invalid file handle");			
			return false;
		}
		else{
			if(theRData != null) 
				StreamUtils.close(theRData);							
			if(theWData != null) 
				StreamUtils.close(theWData);
			
			DataTracker.getInstance().removeStream(fileHandle);
			return true;
		}				
	}
}
