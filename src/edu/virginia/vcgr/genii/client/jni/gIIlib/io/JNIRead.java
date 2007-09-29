package edu.virginia.vcgr.genii.client.jni.gIIlib.io;

import java.io.IOException;
import java.io.InputStream;

import edu.virginia.vcgr.genii.client.byteio.ByteIOConstants;

public class JNIRead {
	synchronized public static String read(Integer fileHandle, Integer offset, Integer length){
		InputStream in = DataTracker.getInstance().getReadStream(fileHandle);
		StringBuffer toReturn = new StringBuffer();
		int read = 0;
		int totalRead=0;			
		int bytesToRead = offset + length;
		
		if(in == null){
			System.out.println("Invalid file handle");			
			return null;
		}
						
		byte []data = new byte[ByteIOConstants.PREFERRED_SIMPLE_XFER_BLOCK_SIZE];		
		
		try
		{	
			//Mark for all bytes to read + some buffer
			in.mark(bytesToRead + 100);
			
			do{													
				read = in.read(data, 0, bytesToRead - totalRead);
				if(read > 0){
					totalRead += read;
					String toAdd = new String(data, 0, read);
					toReturn.append(toAdd);
				}
			}
			while(totalRead < bytesToRead);
			
			in.reset();
									
		}catch (IOException e) {			
			e.printStackTrace();
		}			
				
		return toReturn.toString().substring(offset);		
	}
}