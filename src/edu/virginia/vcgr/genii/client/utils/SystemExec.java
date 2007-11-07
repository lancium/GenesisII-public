package edu.virginia.vcgr.genii.client.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.morgan.util.io.StreamUtils;

public class SystemExec
{
	static public String executeForOutput(String []command)
		throws IOException
	{
		InputStream in = null;
		Process proc = null;
		
		try
		{
			proc = Runtime.getRuntime().exec(command);
			proc.getErrorStream().close();
			proc.getOutputStream().close();
			
			in = proc.getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			return reader.readLine();
		}
		finally
		{
			StreamUtils.close(in);
			
			proc.destroy();
		}
	}
}