package edu.virginia.vcgr.genii.client.cmd;

import java.io.Reader;
import java.io.Writer;

import edu.virginia.vcgr.genii.client.cmd.tools.ToolCategory;

public interface ITool
{
	public String usage();
	public String description();
	public boolean isHidden();
	public ToolCategory getCategory();
	public String getManPage();
	
	public void addArgument(String argument) throws ToolException;
	
	public int run(Writer out, Writer err, Reader in)
		throws Throwable;
}