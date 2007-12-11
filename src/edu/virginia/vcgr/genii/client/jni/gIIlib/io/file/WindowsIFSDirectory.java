package edu.virginia.vcgr.genii.client.jni.gIIlib.io.file;

import java.io.IOException;
import java.util.ArrayList;

import edu.virginia.vcgr.genii.client.rns.RNSPath;

public class WindowsIFSDirectory extends WindowsIFSResource
{			
	public WindowsIFSDirectory(RNSPath fullPath) 		
	{		
		myPath = fullPath;
	}

	public void close() throws IOException {
		//By default don't need to do anything
	}
	
	public ArrayList<String> getInfo(){
		return null;
	}
}
