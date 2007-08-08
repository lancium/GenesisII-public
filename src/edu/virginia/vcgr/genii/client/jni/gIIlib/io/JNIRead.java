package edu.virginia.vcgr.genii.client.jni.gIIlib.io;

import java.io.IOException;
import java.io.InputStream;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;

public class JNIRead {
	public static String read(Integer fileHandle, Integer offset, Integer length){
		InputStream in = DataTracker.getInstance().getReadStream(fileHandle);
		StringBuffer toReturn = new StringBuffer();
		int read;
		
		if(in == null){
			System.out.println("Invalid file handle");			
			return null;
		}
						
		byte []data = new byte[ByteIOConstants.PREFERRED_SIMPLE_XFER_BLOCK_SIZE];		
		
		try
		{			
			read = in.read(data, offset, length);
			String toAdd = new String(data, 0, read);	
			toReturn.append(toAdd);				
		}catch (IOException e) {			
			e.printStackTrace();
		}
		
		return toReturn.toString();		
	}
}