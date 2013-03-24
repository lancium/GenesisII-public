package edu.virginia.vcgr.genii.container.jsdl;

import org.ggf.jsdl.FileSystemTypeEnumeration;

public class ScratchFilesystem extends Filesystem
{
	static final long serialVersionUID = 0L;

	static final public String FILESYSTEM_NAME = "SCRATCH";
	static final public FileSystemTypeEnumeration FILESYSTEM_TYPE = FileSystemTypeEnumeration.spool;

	ScratchFilesystem()
	{
		super(FILESYSTEM_NAME, FILESYSTEM_TYPE);
	}

	@Override
	public String toString()
	{
		return FILESYSTEM_NAME;
	}
}