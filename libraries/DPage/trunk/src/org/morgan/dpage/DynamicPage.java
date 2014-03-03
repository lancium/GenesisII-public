package org.morgan.dpage;

import java.io.IOException;
import java.io.PrintStream;

public interface DynamicPage {
	public void generatePage(PrintStream out) throws IOException;
}