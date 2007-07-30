package edu.virginia.vcgr.genii.client.jni.gIIlib.io;

import java.io.IOException;
import java.io.OutputStream;


public class JNIWrite {
	public static Integer write(Integer fileHandle, String data, Integer offset){
		OutputStream out = DataTracker.getInstance().getWriteStream(fileHandle);		
		
		if(out == null){
			System.out.println("Invalid file handle");			
			return -1;
		}
		try
		{			
			out.write(data.getBytes(), offset, data.length());			
		}catch (IOException e) {			
			e.printStackTrace();
			return -1;
		}
		return data.length();
	}
}
