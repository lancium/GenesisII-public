package edu.virginia.vcgr.genii.client.jni.gIIlib.deprecated;

import edu.virginia.vcgr.genii.client.jni.gIIlib.JNILibraryBase;
import edu.virginia.vcgr.genii.client.rns.RNSException;
import edu.virginia.vcgr.genii.client.rns.RNSPath;
import edu.virginia.vcgr.genii.client.rns.RNSPathAlreadyExistsException;
import edu.virginia.vcgr.genii.client.rns.RNSPathDoesNotExistException;
import edu.virginia.vcgr.genii.client.rns.RNSPathQueryFlags;
import edu.virginia.vcgr.genii.client.rns.RNSUtilities;

public class JNIRmTool extends JNILibraryBase {

	public static Boolean remove(String target, Boolean recursive, Boolean force){
		
		tryToInitialize();
		
		try{
			RNSPath path = RNSPath.getCurrent();				
			return rm(path, target, recursive, force);
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean rm(RNSPath currentPath,
			String filePath, boolean recursive, boolean force) 
	throws RNSPathDoesNotExistException, RNSPathAlreadyExistsException, 
		RNSException			
	{
		RNSPath file = currentPath.lookup(
			filePath, RNSPathQueryFlags.MUST_EXIST);
		rm(file, recursive, force);
		return true;
	}
	
	public static void rm(RNSPath path, boolean recursive, 
			boolean force) throws RNSException
		{
			try
			{
				if (recursive)
					RNSUtilities.recursiveDelete(path);
				else
					path.delete();
			}
			catch (RNSException re)
			{
				if (force)
				{
					System.out.println("Forcing removal after exception");					
					path.unlink();
				} else
					throw re;
			}
		}
}
