package edu.virginia.vcgr.genii.client.configuration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OGRSH
{
	static final private Pattern OGRSH_SHIM_SCRIPT_PATTERN = Pattern.compile(
		"^shim-([^.]+)\\.sh$");
	
	private File _ogrshDir;
	
	OGRSH(File ogrshDir)
	{
		_ogrshDir = ogrshDir;
	}
	
	public File ogrshDir()
	{
		return _ogrshDir;
	}
	
	public Map<String, OGRSHVersion> getInstalledVersions()
	{
		Map<String, OGRSHVersion> installedVersions =
			new HashMap<String, OGRSHVersion>();
				
		if (_ogrshDir.exists() && _ogrshDir.isDirectory())
		{
			File []entries = _ogrshDir.listFiles();
			if (entries != null)
			{
				for (File entry : entries)
				{
					Matcher matcher = OGRSH_SHIM_SCRIPT_PATTERN.matcher(
						entry.getName());
					if (matcher.matches())
					{
						String version = matcher.group(1);
						installedVersions.put(version, 
							new OGRSHVersion(version, entry));
					}
				}
			}
		}
		
		return installedVersions;
	}
}