package edu.virginia.vcgr.genii.ui.shell;

import java.io.PrintWriter;

public interface Display
{
	public void start();

	public PrintWriter output();

	public PrintWriter error();

	public PrintWriter header();

	public PrintWriter command();
}