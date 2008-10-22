package edu.virginia.vcgr.genii.client.cmd;

import java.io.Reader;
import java.io.Writer;

public interface ITool
{
	public String usage();
	public String description();
	public boolean isHidden();
	
	public void addArgument(String argument) throws ToolException;
	
	public int run(Writer out, Writer err, Reader in)
		throws Throwable;
}