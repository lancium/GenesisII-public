package edu.virginia.vcgr.genii.container.bes;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.virginia.vcgr.genii.client.configuration.ConfigurationManager;

public class OGRSHUtils
{
	static private final Pattern SHIM_PATTERN = 
		Pattern.compile("^shim-([^.]+).sh$");
	
	static public Collection<String> ogrshVersionsSupported()
	{
		Collection<String> ret = new LinkedList<String>();
		
		File installDir = new File(ConfigurationManager.getInstallDir());
		File ogrshDir = new File(installDir, "OGRSH");
		if (!ogrshDir.exists() || !ogrshDir.isDirectory())
			return ret;
		
		for (String filename : ogrshDir.list())
		{
			Matcher matcher = SHIM_PATTERN.matcher(filename);
			if (matcher.matches())
			{
				ret.add(matcher.group(1));
			}
		}
		
		return ret;
	}
}