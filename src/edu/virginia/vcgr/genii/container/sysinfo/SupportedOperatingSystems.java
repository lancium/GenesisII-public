package edu.virginia.vcgr.genii.container.sysinfo;

public enum SupportedOperatingSystems
{
	WINDOWS(),
	LINUX(),
	MACOSX();
	
	static public SupportedOperatingSystems current()
	{
		System.err.format(
			"I'm using an OLD method for detecting OS (%s).\n" +
			"PLEASE REPLACE ME!\n", 
			SupportedOperatingSystems.class.getName());
		
		String os = System.getProperty("os.name");
		if (os == null)
			throw new RuntimeException(
				"Unable to determine current operating system type.");
		
		if (os.startsWith("Windows"))
			return WINDOWS;
		else if (os.equals("Linux"))
			return LINUX;
		else if (os.equals("Mac OS X"))
			return MACOSX;
		else
			throw new RuntimeException(
				"Unsupported operating system detected (" + os + ").");
	}
}