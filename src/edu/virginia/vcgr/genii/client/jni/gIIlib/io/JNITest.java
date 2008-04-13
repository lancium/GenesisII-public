package edu.virginia.vcgr.genii.client.jni.gIIlib.io;

import java.util.ArrayList;

import edu.virginia.vcgr.genii.client.jni.gIIlib.JNILibraryBase;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles.WindowsFileHandle;
import edu.virginia.vcgr.genii.client.jni.gIIlib.io.handles.WindowsResourceHandle;

public class JNITest {

	public static void main(String[] args) throws Exception {
		testReadWriteCachedInfo();

		
	}
	
	/** 
	 * Tests Create and Removes of files in a directory
	 */
	public static void testCreateOpenDeleteDirListing(){
		ArrayList<String> returnedDir = JNIOpen.open("/home/sosa", WindowsResourceHandle.OPEN, 
				WindowsResourceHandle.FILE_WRITE_DATA, true);
		
		ArrayList<String> returned = JNIOpen.open("/home/sosa/createMe.txt", WindowsResourceHandle.CREATE, 
				WindowsResourceHandle.FILE_WRITE_DATA, false);		
		
		ArrayList<String> returnedOne = JNIDirectoryListing.getDirectoryListing(Integer.parseInt(returnedDir.get(0)), ".*");
		
		int counter=1;
		System.out.println("/home/sosa :");
		for(String name : returnedOne){
			if(counter % 4 == 0){
				System.out.println(name);
				counter = 1;
			}
			else{
				counter++;
			}
		}
		
		JNIClose.close(Integer.parseInt(returned.get(0)), true);
		
		ArrayList<String> returnedTwo = JNIDirectoryListing.getDirectoryListing(Integer.parseInt(returnedDir.get(0)), ".*");
		System.out.println("/home/sosa :");
		for(String name : returnedTwo){
			if(counter % 4 == 0){
				System.out.println(name);
				counter = 1;
			}
			else{
				counter++;
			}
		}
		
	}
	
	/**
	 * Tests Reads and Writes with respect to each other and cached RNS information
	 * @throws Exception
	 */
	public static void testReadWriteCachedInfo() throws Exception{
		JNILibraryBase.tryToInitialize();
		
		WindowsFileHandle myFile = new WindowsFileHandle("/home/sosa/createMe.txt", WindowsResourceHandle.OPEN, 
				WindowsResourceHandle.FILE_WRITE_DATA);		
		
		myFile.write("I like to eat my's tacos a lot\n".getBytes(), myFile.getCachedInformation().size);
		myFile.close(false);
		
		WindowsFileHandle myFile2 = new WindowsFileHandle("/home/sosa/createMe.txt", WindowsResourceHandle.OPEN, 
				WindowsResourceHandle.FILE_WRITE_DATA | WindowsResourceHandle.FILE_READ_DATA);		
		
		String returned = new String(myFile2.read(0, myFile2.getCachedInformation().size));
		
		System.out.println(returned);
		
		myFile2.close(false);
	}
}
