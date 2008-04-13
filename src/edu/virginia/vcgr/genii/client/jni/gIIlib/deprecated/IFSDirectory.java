package edu.virginia.vcgr.genii.client.jni.gIIlib.deprecated;

import java.io.IOException;
import java.util.ArrayList;

import edu.virginia.vcgr.genii.client.rns.RNSPath;

public class IFSDirectory extends IFSResource
{			
	public IFSDirectory(RNSPath fullPath) 		
	{		
		isDirectory = true;
		myPath = fullPath;
	}

	public void close() throws IOException {
		//By default don't need to do anything
	}
	
	public ArrayList<String> getInfo(){
		return null;
	}
}
