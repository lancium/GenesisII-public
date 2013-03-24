package edu.virginia.vcgr.genii.container.exportdir.lightweight;

public interface VExportEntry
{
	public String getName();

	boolean isDirectory();

	boolean isFile();
}
