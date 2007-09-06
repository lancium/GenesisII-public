package edu.virginia.vcgr.genii.client.jni.gIIlib.io;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import edu.virginia.vcgr.genii.client.byteio.ByteIOInputStream;
import edu.virginia.vcgr.genii.client.byteio.ByteIOOutputStream;
import edu.virginia.vcgr.genii.client.jni.gIIlib.JNIGetInformationTool;
import edu.virginia.vcgr.genii.client.jni.gIIlib.JNILibraryBase;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;

public class JNIOpen extends JNILibraryBase{
	
	public static ArrayList<String> open(String fileName, Boolean create, Boolean read, 
			Boolean write){
		InputStream theRData = null;
		OutputStream theWData = null;
		ArrayList<String> toReturn = new ArrayList<String>();
		DataTracker tracker = DataTracker.getInstance();
		int fileHandle = -1;
		
		//Check if path is valid
		if(!JNIGetInformationTool.checkIfValidPath(fileName)){
			return null;
		}
				
		try{
			
			tryToInitialize();				
			
			RNSPath current = RNSPath.getCurrent();
			RNSPath filePath;			
			
			if(create){
				filePath =  current.lookup(fileName, RNSPathQueryFlags.DONT_CARE);									
			}
			else{
				filePath = current.lookup(fileName, RNSPathQueryFlags.MUST_EXIST);
			}
			
			//Can't read directories
			if(filePath != null && filePath.exists() && 
					filePath.isDirectory()){
				read = false;
			}
			
			fileHandle = tracker.atomicGetAndIncrementHandle();
			
			if(write){
				theWData = new ByteIOOutputStream(filePath);
				tracker.putStream(fileHandle, theWData);
			}
			if(read){
				theRData = new ByteIOInputStream(filePath);
				tracker.putStream(fileHandle, theRData);
			}
			toReturn.add(String.valueOf(fileHandle));
			toReturn.addAll(JNIGetInformationTool.getInformation(fileName));
			
			return toReturn;	
		}
		catch(Exception e){
			e.printStackTrace();
			tracker.removeStream(fileHandle);
			return null;						
		}				
	}
}
