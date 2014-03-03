package edu.virginia.vcgr.genii.container.rfork;

public class RForkUtils
{

	public static String formForkPathFromPath(String path, String entryName)
	{
		if (path.endsWith("/"))
			return path + entryName;
		return path + "/" + entryName;
	}

}
