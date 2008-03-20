package edu.virginia.vcgr.genii.container.sysinfo;

public enum SupportedOperatingSystems
{
	WINDOWS(),
	LINUX();
	
	static public SupportedOperatingSystems current()
	{
		String os = System.getProperty("os.name");
		if (os == null)
			throw new RuntimeException(
				"Unable to determine current operating system type.");
		
		if (os.startsWith("Windows"))
			return WINDOWS;
		else if (os.equals("Linux"))
			return LINUX;
		else
			throw new RuntimeException(
				"Unsupported operating system detected (" + os + ").");
	}
}