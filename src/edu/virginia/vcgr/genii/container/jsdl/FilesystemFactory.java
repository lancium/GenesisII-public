package edu.virginia.vcgr.genii.container.jsdl;

import org.ggf.jsdl.FileSystemTypeEnumeration;

import edu.virginia.vcgr.genii.client.jsdl.InvalidJSDLException;
import edu.virginia.vcgr.genii.client.jsdl.JSDLException;

public class FilesystemFactory
{
	static private Filesystem getScratchFilesystem(FileSystemTypeEnumeration type, String mountPoint, String mountSource)
		throws JSDLException
	{
		if (type != FileSystemTypeEnumeration.spool)
			throw new InvalidJSDLException(String.format("%s filesytem must have a file system type of \"%s\".",
				ScratchFilesystem.FILESYSTEM_NAME, ScratchFilesystem.FILESYSTEM_TYPE));

		if (mountPoint != null)
			throw new InvalidJSDLException(String.format("%s filesystem cannot have a mount point element.",
				ScratchFilesystem.FILESYSTEM_NAME));

		if (mountSource != null)
			throw new InvalidJSDLException(String.format("%s filesystem cannot have a mount source element.",
				ScratchFilesystem.FILESYSTEM_NAME));

		return new ScratchFilesystem();
	}

	static private Filesystem getGridFilesystem(String name, FileSystemTypeEnumeration type, String mountPoint,
		String mountSource) throws JSDLException
	{
		if (type != GridFilesystem.FILESYSTEM_TYPE)
			throw new InvalidJSDLException(String.format("Grid filesystem %s must have a file system type of \"%s\".", name,
				GridFilesystem.FILESYSTEM_TYPE));

		if (mountPoint != null)
			throw new InvalidJSDLException(String.format("Grid filesystem %s cannot have a mount point element.", name));

		return new GridFilesystem(name, mountSource);
	}

	static public Filesystem getFilesystem(String name, FileSystemTypeEnumeration type, String mountPoint, String mountSource)
		throws JSDLException
	{
		if (name == null)
			throw new IllegalArgumentException("File systems cannot have a null name.");

		if (name.equals(ScratchFilesystem.FILESYSTEM_NAME))
			return getScratchFilesystem(type, mountPoint, mountSource);

		if (mountSource != null) {
			if (GridFilesystem.MOUNT_SOURCE_PATTERN.matcher(mountSource).matches())
				return getGridFilesystem(name, type, mountPoint, mountSource);
		}

		throw new InvalidJSDLException(String.format("Don't know how to handle file system \"%s\".", name));
	}
}