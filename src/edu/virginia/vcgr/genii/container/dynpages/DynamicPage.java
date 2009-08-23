package edu.virginia.vcgr.genii.container.dynpages;

import java.io.IOException;
import java.io.PrintStream;

public interface DynamicPage
{
	public void generate(PrintStream ps) throws IOException;
}