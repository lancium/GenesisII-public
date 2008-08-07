package edu.virginia.vcgr.genii.client.spmd;

import java.util.List;

public interface SPMDTranslator
{
	public String getProviderName();
	
	public List<String> translateCommandLine(List<String> commandLine)
		throws SPMDException;
}
