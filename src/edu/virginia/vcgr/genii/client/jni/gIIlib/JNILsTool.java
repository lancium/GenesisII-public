package edu.virginia.vcgr.genii.client.jni.gIIlib;

import java.util.ArrayList;

import edu.virginia.vcgr.genii.client.context.ContextManager;
import edu.virginia.vcgr.genii.client.context.ICallingContext;
import edu.virginia.vcgr.genii.client.resource.TypeInformation;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;

public class JNILsTool extends JNILibraryBase
{
	public static ArrayList getDirectoryListing() {
		if(!isInitialized){
			initialize();
		}
		
		ArrayList<String> directoryListing = null;
		try
		{								
			directoryListing = new ArrayList<String>();
			ICallingContext ctxt = ContextManager.getCurrentContext();
			String[] arguments = new String[] {"."};
			ArrayList<RNSPath> targets = new ArrayList<RNSPath>();

			for (String arg : arguments)
			{
				RNSPath []paths = ctxt.getCurrentPath().list(arg, 
						RNSPathQueryFlags.MUST_EXIST);

				for (RNSPath path : paths)
				{
					targets.add(path);
				}
			}

			ArrayList<RNSPath> dirs = new ArrayList<RNSPath>();

			for (RNSPath path : targets)
			{
				TypeInformation type = new TypeInformation(
						path.getEndpoint());
				if (!type.isRNS()){
					directoryListing.add(path.getName());
				}
				else
				{
					dirs.add(path);
				}
			}				
			for (RNSPath path : dirs)
			{
				RNSPath []entries = null;
				entries = path.list(".*", RNSPathQueryFlags.DONT_CARE);


				if (entries.length > 1 || entries[0].exists())
				{
					for (RNSPath entry : entries)
					{
						directoryListing.add(entry.getName());
					}
				}								
			}
		}
		catch (Throwable t)
		{
			t.printStackTrace();
			directoryListing = null;
		}
		
		return directoryListing;
	}
}