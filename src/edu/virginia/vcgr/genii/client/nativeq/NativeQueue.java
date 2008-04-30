package edu.virginia.vcgr.genii.client.nativeq;

import java.io.File;
import java.util.Properties;

public interface NativeQueue
{
	static public final String SUBMIT_SCRIPT_NAME_PROPERTY =
		"edu.virginia.vcgr.genii.client.nativeq.submit-script-name";
	static public final String BASH_BINARY_PATH_PROPERTY =
		"edu.virginia.vcgr.genii.client.nativeq.bash-binary";
	
	public String getProviderName();
	
	public NativeQueueConnection connect(
		File workingDirectory,
		Properties connectionProperties) throws NativeQueueException;
}