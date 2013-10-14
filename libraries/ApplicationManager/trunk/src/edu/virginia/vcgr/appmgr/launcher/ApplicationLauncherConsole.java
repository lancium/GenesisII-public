package edu.virginia.vcgr.appmgr.launcher;

import java.io.PrintStream;
import java.util.Calendar;

import edu.virginia.vcgr.appmgr.version.Version;

public interface ApplicationLauncherConsole
{
	public Version currentVersion();

	public Calendar lastUpdated();

	public boolean doUpdates(PrintStream log);
}