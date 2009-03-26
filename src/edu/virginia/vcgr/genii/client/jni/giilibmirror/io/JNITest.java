package edu.virginia.vcgr.genii.client.jni.giilibmirror.io;

import java.util.ArrayList;

import edu.virginia.vcgr.genii.client.jni.giilibmirror.JNILibraryBase;
import edu.virginia.vcgr.genii.client.jni.giilibmirror.io.handles.WindowsFileHandle;
import edu.virginia.vcgr.genii.client.jni.giilibmirror.io.handles.WindowsResourceHandle;

public class JNITest {

	public static void main(String[] args) throws Exception {
		testOpenStuff();

		
	}
	
	public static void testOpenStuff(){
		
		//Open /home 1 0 0
		ArrayList<String> homeDir = JNIOpen.open("/home", WindowsResourceHandle.OPEN, 
				0, false);
		
		JNIDirectoryListing.getDirectoryListing(Integer.parseInt(homeDir.get(0)), "*");
		
		JNIClose.close(Integer.parseInt(homeDir.get(0)), false);
				
		
		ArrayList<String> markDir = JNIOpen.open("/home/morgan", WindowsResourceHandle.OPEN, 
				0, false);
		
		JNIDirectoryListing.getDirectoryListing(Integer.parseInt(markDir.get(0)), "*");
		
		JNIClose.close(Integer.parseInt(markDir.get(0)), false);
						
		//Open /home 1 0 0 again
		ArrayList<String> homeDir2 = JNIOpen.open("/home", WindowsResourceHandle.OPEN, 
				0, false);
		
		JNIDirectoryListing.getDirectoryListing(Integer.parseInt(homeDir2.get(0)), "*");
		
		JNIClose.close(Integer.parseInt(homeDir2.get(0)), false);
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
		
		//String returned = new String(myFile2.read(0, getCachedInformation().size)));		
		//System.out.println(returned);
		
		myFile2.close(false);
	}
}
