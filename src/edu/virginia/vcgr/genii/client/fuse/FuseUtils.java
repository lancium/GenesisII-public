package edu.virginia.vcgr.genii.client.fuse;

import java.io.File;

import edu.virginia.vcgr.genii.client.utils.SystemExec;

public class FuseUtils
{
	static public String checkForFuserMount()
	{
		File f = SystemExec.findExecutableInPath("fusermount");
		if (f == null)
			return "Unable to locate fusermount binary.";
		
		return null;
	}
	
	static private String checkForFuseDevice()
	{
		File f = new File("/dev/fuse");
		if (!f.exists())
			return "Couldn't locate /dev/fuse.";
		if (!f.canRead())
			return "Can't read /dev/fuse.";
		if (!f.canWrite())
			return "Can't write /dev/fuse.";
		
		return null;
	}
	
	static public String supportsFuse()
	{
		String msg = checkForFuserMount();
		if (msg != null)
			return msg;
		
		msg = checkForFuseDevice();
		if (msg != null)
			return msg;
		
		return null;
	}
}