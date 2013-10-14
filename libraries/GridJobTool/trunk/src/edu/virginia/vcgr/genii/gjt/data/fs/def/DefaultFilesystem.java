package edu.virginia.vcgr.genii.gjt.data.fs.def;

import edu.virginia.vcgr.genii.gjt.data.fs.AbstractFilesystem;
import edu.virginia.vcgr.genii.gjt.data.fs.FilesystemType;

public class DefaultFilesystem extends AbstractFilesystem
{
	static public final String COMMON_NAME = "Job Directory";
	static public final String JSDL_NAME = "";
	static public final String DESCRIPTION = "The default JSDL filesystem.";

	static public final boolean CAN_EDIT = false;

	DefaultFilesystem()
	{
		super(FilesystemType.Default);
	}
}